package org.lambd;

import org.lambd.annotation.Annotation;
import org.lambd.annotation.AnnotationType;
import org.lambd.obj.*;
import org.lambd.pointer.Pointer;
import org.lambd.pointer.PointerToSet;
import org.lambd.transformer.SpStmt;
import org.lambd.transition.*;
import org.lambd.wrapper.Wrapper;
import soot.*;
import soot.jimple.*;
import soot.tagkit.VisibilityAnnotationTag;

import java.util.*;
import java.util.stream.Collectors;

public class SpMethod implements Wrapper {
    private SootMethod sootMethod;
    public final String name;
    private Summary summary;
    private ObjManager manager;
    private PointerToSet ptset;
    private List<SpStmt> stmts = new ArrayList<>();
    private List<Annotation> annotionList = new ArrayList<>();
    // 第i个参数可能的类型
    private Map<Integer, Set<SootClass>> mayClassMap = new HashMap<>();
    private State state;
    public SpMethod caller;
    public SpMethod(SootMethod sootMethod) {
        this.name = sootMethod.getName();
        this.sootMethod = sootMethod;
        state = State.UNINITIALZED;
    }
    public void visit() {
        if (state == State.UNINITIALZED) {
            state = State.VISITED;
            summary = new Summary(this);
            summary.newContext();
            ptset = new PointerToSet(this);
            manager = new OneObjManager(this, ptset);
        } else {
            // 后面再次访问时，初始化context
            summary.newContext();
        }
    }
    public boolean visited() {
        return state == State.VISITED || state == State.FINISHED;
    }
    public void addStmts(SpStmt stmt) {
        stmts.add(stmt);
    }
    public List<SpStmt> getStmts() {
        return stmts;
    }
    public void addMayClass(int index, SootClass mayClass) {
        mayClassMap.computeIfAbsent(index, n -> new HashSet<>()).add(mayClass);
    }
    public Set<SootClass> getMayClass(int index) {
        return mayClassMap.getOrDefault(index, Collections.emptySet());
    }
    public void addAnnotation(Annotation an) {
        annotionList.add(an);
    }
    public List<Annotation> getAnnotionList() {
        return annotionList;
    }
    public boolean checkAnnotation() {
        List<AnnotationType> conditions = annotionList.stream().map(Annotation::getAnnotationType).toList();
        if (conditions.contains(AnnotationType.POST) && conditions.contains(AnnotationType.PATH))
            return true;
        if (conditions.contains(AnnotationType.SHELLMETHOD))
            return true;
        if (conditions.contains(AnnotationType.AUTOWIRED))
            return true;
        return false;
    }
    public String getAnnotation() {
        return annotionList.stream()
                .map(Annotation::getAnnotationType)
                .map(AnnotationType::getType)
                .collect(Collectors.joining());
    }
    private Value getParameter(SpStmt spStmt, int i) {
        Stmt stmt = spStmt.getStmt();
        if (stmt instanceof AssignStmt assignStmt) {
            if (i == -2)
                return assignStmt.getLeftOp();
            Value rhs = assignStmt.getRightOp();
            if (rhs instanceof InvokeExpr invoke) {
                if (i == -1 && invoke instanceof InstanceInvokeExpr instanceInvoke) {
                    return instanceInvoke.getBase();
                }
                try {
                    return invoke.getArg(i);
                } catch (Exception e) {
                    return null;
                }
            }
        } else if (stmt instanceof InvokeStmt invokeStmt) {
            if (i == -2)
                return null;
            InvokeExpr invokeExpr = invokeStmt.getInvokeExpr();
            if (i == -1 && invokeExpr instanceof InstanceInvokeExpr instanceInvoke) {
                return instanceInvoke.getBase();
            }
            return invokeExpr.getArg(i);
        }
        return null;
    }
    // 在返回caller的lhs时，需要做一个condition过滤
    public void handleTransition(SpStmt stmt, int from, int to, Weight w) {
        Value fromVar = getParameter(stmt, from);
        Value toVar = getParameter(stmt, to);
        if (fromVar == toVar)
            return;
        Type type = null;
        if (to == -2 && toVar != null && toVar.getType() instanceof RefType rt) {
            type = rt;
        }
        if (fromVar instanceof Local l1 && toVar instanceof Local l2)
            ptset.update(l1, l2, w, stmt, type);
    }
    public void handleCollectionOp(SpStmt stmt, int from, int to, int op) {
        // kind: 0->pointer copy; 1->deep copy; 2->list.add; 3->iterator(); 4->iterator.next
        Value fromVar = getParameter(stmt, from);
        Value toVar = getParameter(stmt, to);
        if (fromVar == toVar)
            return;
        if (fromVar instanceof Local l1 && toVar instanceof Local l2) {
            Pointer fromPointer = ptset.getVarPointer(l1);
            Pointer toPointer = ptset.getVarPointer(l2);
            if (op == 2)
                toPointer.addChild(fromPointer);
            else if (op == 3)
                toPointer.setIterator(fromPointer);
            else {
                for (Pointer child : fromPointer.getIterator().getChildren()) {
                    toPointer.copyFrom(child);
                }
            }

        }
    }
    public void handleLoadTransition(SpStmt stmt) {
        // packages(String[])($7)
        Local arg0 = (Local) getParameter(stmt,0);
        Set<String> packageName = ptset.getArrayString(arg0);
        SootWorld.v().analyzePackage(packageName);
    }

    /**
     * get the var of arg index, and var.fields exists and can hold string
     * @param stmt current invoke stmt
     * @param sink sink description
     * @param index arg index can transfer to sink
     * @param w fields of arg can transfer to sink
     */
    public void handleSink(SpStmt stmt, String sink, int index, Weight w) {
        Value var = getParameter(stmt, index);
        if (var instanceof Local l)
            ptset.genSink(sink, w, l, stmt);
    }
    public void handleReturn(SpStmt stmt, RefType type) {
        Value val = getParameter(stmt, -2);
        if (val instanceof Local lvar) {
            Obj obj = new SourceObj(type, stmt);
            ptset.getVarPointer(lvar).add(obj);
        }
    }
    public void analyzeAnnotation() {
        VisibilityAnnotationTag tag = (VisibilityAnnotationTag) sootMethod.getTag("VisibilityAnnotationTag");
        if (tag != null) {
            tag.getAnnotations().forEach(anno -> {
                Annotation annotation = Annotation.extractAnnotation(anno);
                if (annotation != null)
                    addAnnotation(annotation);
            });
        }
    }
    public String getName() {
        return name;
    }
    public SootMethod getSootMethod() {
        return sootMethod;
    }
    public String toString() {
        return sootMethod.toString();
    }
    public ObjManager getManager() {
        return manager;
    }
    public Summary getSummary() {
        return summary;
    }
    public PointerToSet getPtset() {
        return ptset;
    }
    public void finish() {
        state = State.FINISHED;
    }
    public boolean isFinished() {
        return state == State.FINISHED;
    }
}
enum State {
    UNINITIALZED,
    VISITED,
    FINISHED,
}