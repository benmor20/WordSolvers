package wordsolvers.structs.parsers;

public interface Parser<T> {
    String serialize(T obj);
    T parse(String str);
}
