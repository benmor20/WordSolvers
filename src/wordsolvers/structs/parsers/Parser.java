package wordsolvers.structs.parsers;

public abstract class Parser<T> {
    private final Class<T> objClass;

    public Parser(Class<T> objClass) {
        this.objClass = objClass;
    }

    public abstract String serialize(T obj);
    public abstract T parse(String str);

    protected void throwParseError(String str, String message, int index) {
        throw new IllegalArgumentException("Could not parse " + this.objClass.getName() + " from " + str + ": "
                + message + " (index " + index + ").");
    }
}
