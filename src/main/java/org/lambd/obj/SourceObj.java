package org.lambd.obj;

import org.lambd.transformer.SpStmt;
import soot.Type;

public class SourceObj extends RealObj {
    public SourceObj(Type type, SpStmt stmt) {
        super(type, stmt);
    }
}
