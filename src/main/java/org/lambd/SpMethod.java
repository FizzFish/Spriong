package org.lambd;

import org.lambd.anonotation.Annotation;
import org.lambd.anonotation.AnnotationType;
import org.lambd.obj.*;
import org.lambd.pointer.PointerToSet;
import org.lambd.transition.*;
import org.lambd.wrapper.Wrapper;
import soot.*;
import soot.jimple.*;
import soot.tagkit.VisibilityAnnotationTag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpMethod implements Wrapper {
    private SootMethod sootMethod;
    public final String name;
    private Summary summary;
    private ObjManager manager;
    private PointerToSet ptset;
    private List<Annotation> annotionList = new ArrayList<>();
    private State state;
    public SpMethod caller;
    public SpMethod(SootMethod sootMethod) {
        this.name = sootMethod.getName();
        this.sootMethod = sootMethod;
        summary = new Summary(this);
        ptset = new PointerToSet(this);
        manager = new OneObjManager(this, ptset);
        state = State.VISITED;
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
    private Value getParameter(Stmt stmt, int i) {
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
    public void handleTransition(Stmt stmt, int from, int to, Weight w) {
        Value fromVar = getParameter(stmt, from);
        Value toVar = getParameter(stmt, to);
        if (fromVar == toVar)
            return;
        if (fromVar instanceof Local l1 && toVar instanceof Local l2)
            ptset.update(l1, l2, w, stmt);
    }
    public void handleLoadTransition(Stmt stmt) {
        // packages(String[])($7)
        Local arg0 = (Local) getParameter(stmt,0);
        SootWorld.v().analyzePackage(ptset.getArrayString(arg0));
    }

    /**
     * get the var of arg index, and var.fields exists and can hold string
     * @param stmt current invoke stmt
     * @param sink sink description
     * @param index arg index can transfer to sink
     * @param w fields of arg can transfer to sink
     */
    public void handleSink(Stmt stmt, String sink, int index, Weight w) {
        Value var = getParameter(stmt, index);
        if (var instanceof Local l)
            ptset.genSink(sink, w, l, stmt);
    }
    public void handleReturn(Stmt stmt, RefType type) {
        Value lhs = getParameter(stmt, -2);
        if (lhs instanceof Local l)
            ptset.updateLhs(l, type, stmt);
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
    STRANGE,
    VISITED,
    FINISHED,
}