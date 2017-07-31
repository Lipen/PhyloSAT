package beepp.expression;

/**
 * @author Vyacheslav Moklev
 */
public abstract class Variable {
    final String name;

    Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract String getDeclaration();
}
