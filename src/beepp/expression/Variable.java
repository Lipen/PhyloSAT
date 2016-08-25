package beepp.expression;

/**
 * @author Vyacheslav Moklev
 */
public abstract class Variable {
    protected String name;

    public Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
