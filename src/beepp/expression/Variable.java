package beepp.expression;

/**
 * @author Moklev Vyacheslav
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
