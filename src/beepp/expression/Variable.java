package beepp.expression;

/**
 * @author Moklev Vyacheslav
 */
public abstract class Variable {
    private String name;

    public Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
