package errores;

import repositorioMem.Repositorio;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class repofallido<T> {


// persistir un obj p una sola clase
protected Map<Long, T> data = new HashMap<>();

protected AtomicLong idGenerator = new AtomicLong(); // -> gen id
// Repositorio Global -> p guardar una entidad y las relacionadas
private static Map<Class<?>, Repositorio<?>>  repositorios  = new HashMap<>();

public static <E> Repositorio<E> getRepositorio(Class<E> clazz) {
    return (Repositorio<E>) repositorios.computeIfAbsent(clazz, c -> new Repositorio<>()); // busc dentro del (Repo Gl) si ya existe un rep con la clase
}                                                                                                      // y lo devolvemos , si no generamos uno nuevo con clave clazz.


// save recursivo/cascada Global-> guarda todas las entidades relacionadas
// visitados cuenta cada obj persistido pr evitar recurision inf (bidireccionalidad)

private T saveRecursive(T entity, Set<Object> visitados) {
    if (entity == null) return null;

    if (visitados.contains(entity)) {
        return entity; // ya se guardo
    }
    visitados.add(entity);

    // Persisto esta entidad -> con el save normal(entidad raiz)
    T persisted = save(entity);

    // obtenemos los atributos e iteramos en busca de una entidad o coleccion que persistir
    for (Field field : entity.getClass().getDeclaredFields()) {
        field.setAccessible(true);
        try {
            Object valor = field.get(entity);
            if (valor == null) continue; // si es null no trato de persisir -> sig Field

            // Si es colección, persisto cada elemento
            if (valor instanceof Collection<?>) {
                for (Object obj : (Collection<?>) valor) {
                    guardarEntidadRelacionada(obj,visitados);
                }
            }
            // Si es entidad simple, persisto también
            else if (esEntidad(valor)) {  // si es campo atributo simpl-> sig Camp
                guardarEntidadRelacionada(valor,visitados);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    return persisted; // devolvemos el repo (global) .
}



// Metodo sobrecargado saveRecursive -> es el que usa el usuario
public T saveRecursive(T entity) {
    return saveRecursive(entity, new HashSet<>());
}


private void guardarEntidadRelacionada(Object obj,Set<Object> visitados) {

    if (obj == null) return; // si es null -> volv a bulcle
    Repositorio repositorio = getRepositorio(obj.getClass()); // obtenemos el repo si exist
    // repositorio.saveRecursive(obj,visitados);

}


// problemas con Reflection No such y Accesible Obj
private boolean esEntidad(Object obj) {  //si no es Obj de los atributos simpl -> inst p persist
    return !(obj instanceof AtomicLong ||
            obj instanceof Long ||
            obj instanceof String ||
            obj instanceof Integer ||
            obj instanceof LocalTime ||
            obj instanceof Boolean);
}



// save simple solo una entidad y que no tenga relaciones
public T save(T entity) {
    Long id = idGenerator.incrementAndGet();
    // Suponiendo que las entidades tienen un método setId
    try {
        String clase;
        Field campoid = entity.getClass().getDeclaredField("setId");
        campoid.setAccessible(true);
        campoid.set(entity, id);
        clase = entity.getClass().getName();
        System.out.println(clase + " con id :" + id);
    } catch (Exception e) {
        e.printStackTrace();
    }

    data.put(id, entity);
    return entity;
}

public Optional<T> findById(Long id) {
    return Optional.ofNullable(data.get(id));
}



public List<T> findAll() {
    return new ArrayList<>(data.values());
}


public Optional<T> genericUpdate(Long id, T updatedEntity) {
    if (!data.containsKey(id)) {
        return Optional.empty();
    }

    try {
        // Establecer el mismo ID en la entidad actualizada para mantener la coherencia
        Method setIdMethod = updatedEntity.getClass().getMethod("setId", Long.class);
        setIdMethod.invoke(updatedEntity, id);

        data.put(id, updatedEntity);
        return Optional.of(updatedEntity);
    } catch (Exception e) {
        e.printStackTrace();
        return Optional.empty();
    }
}

public Optional<T> genericDelete(Long id) {
    if (!data.containsKey(id)) {
        return Optional.empty();
    }
    return Optional.ofNullable(data.remove(id));
}

public List<T> genericFindByField(String fieldName, Object value) {
    List<T> results = new ArrayList<>();
    try {
        for (T entity : data.values()) {
            Method getFieldMethod = entity.getClass().getMethod("get" + capitalize(fieldName));
            Object fieldValue = getFieldMethod.invoke(entity);
            if (fieldValue != null && fieldValue.equals(value)) {
                results.add(entity);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return results;
}

private String capitalize(String str) {
    if (str == null || str.isEmpty()) {
        return str;
    }
    return str.substring(0, 1).toUpperCase() + str.substring(1);
}
}

