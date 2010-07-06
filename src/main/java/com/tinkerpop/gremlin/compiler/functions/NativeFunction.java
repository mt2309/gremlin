package com.tinkerpop.gremlin.compiler.functions;

import com.tinkerpop.gremlin.compiler.Atom;
import com.tinkerpop.gremlin.compiler.GremlinEvaluator;
import com.tinkerpop.gremlin.compiler.VariableLibrary;
import com.tinkerpop.gremlin.compiler.operations.Operation;
import com.tinkerpop.gremlin.compiler.operations.util.DeclareVariable;

import java.util.List;

/**
 * @author Pavel A. Yaskevich
 */
public class NativeFunction implements Function<Object> {

    private String FUNCTION_NAME;

    private List<String> arguments;
    private List<Operation> body;

    public NativeFunction(final String functionName, final List<String> arguments, final List<Operation> body) {
        this.FUNCTION_NAME = functionName;
        this.arguments = arguments;
        this.body = body;
    }

    public Atom<Object> compute(final List<Operation> params) throws RuntimeException {
        if (this.arguments.size() != params.size())
            throw new RuntimeException("Wrong number of arguments (" + params.size() + " of " + this.arguments.size() + ")");

        // cloning variable library
        // all changes in variables will stay inside function + full access to current state of global variables
        VariableLibrary varLib = GremlinEvaluator.getVariableLibrary().clone();

        // mapping arguments to params
        for (int i = 0; i < this.arguments.size(); i++) {
            Atom computedParam = params.get(i).compute();
            DeclareVariable.decalareWithInit(this.arguments.get(i), computedParam);
        }

        Atom result = null;
        for (int i = 0; i < this.body.size(); i++) {
            result = this.body.get(i).compute();
        }

        // setting variable library back to original
        GremlinEvaluator.setVariableLibrary(varLib);

        return result;
    }

    public String getFunctionName() {
        return this.FUNCTION_NAME;
    }

}
