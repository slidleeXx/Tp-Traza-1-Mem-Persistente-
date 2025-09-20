package repositorioMem;

import java.lang.reflect.*;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.invoke.ConstantBootstraps.invoke;

public class Repositorio <T>{


    // persistir un obj p una sola clase
    protected Map<Long,T> data = new HashMap<>();

    protected AtomicLong idGenerator = new AtomicLong(); // -> gen id
    // Repositorio Global -> p guardar una entidad y las relacionadas
    private static Map<Class<?>, Repositorio<?>>  repositorios  = new HashMap<>();

    public static <E> Repositorio<E> getRepositorio(Class<E> clazz) {

        // busc dentro del (Repo Gl) si ya existe un rep con la clase
        // y lo devolvemos , si no generamos uno nuevo con clave clazz.
        return (Repositorio<E>) repositorios.computeIfAbsent(clazz, c -> new Repositorio<>());

}



    // save recursivo/cascada Global-> guarda todas las entidades relacionadas
    // visitados cuenta cada obj persistido pr evitar recurision inf (bidireccionalidad)

    private <E> E saveRecursive(E entity, Set<Object> visitados) {
        if (entity == null || visitados.contains(entity)) return entity;
        visitados.add(entity);

        try {
            // intentamos setear el id solo en entidades del paquete. add metodo
            if (isEntidadPropia(entity.getClass())) {
                Field idField = entity.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                Long id = (Long) idField.get(entity);
                if (id == null) {
                    id = idGenerator.incrementAndGet()+ (Long) idField.get(entity);
                    idField.set(entity, id);
                }
                // persistimos
                Repositorio<Object> repo = (Repositorio<Object>) getRepositorio(entity.getClass());
                repo.data.put(id, entity);

            }

            // Recorremos campos y guardamos relaciones
            for (Field f : entity.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object valor = f.get(entity);
                if (valor == null) continue; // saltamos al siguiente campo

                // guardamos objetos dentro de contenedores
                if (valor instanceof Iterable<?> colleccion) {
                    for (Object sub : colleccion) {
                        if (isEntidadPropia(sub.getClass())) {
                            saveRecursive(sub,visitados);
                        }
                    }
                    // comprobamos que no sea Cl de JDK
                } else if (isEntidadPropia(valor.getClass())) {
                    saveRecursive( valor, visitados);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return entity;
    }


    // Solo clases de mi paquete ignoramos clases del JDK (such y accesible)
    private boolean isEntidadPropia(Class<?> clazz) {
        return clazz.getPackageName().startsWith("entidades");
    }


    // Metodo sobrecargado saveRecursive -> es el que usa el usuario
    public <E> E saveRecursive(E entity) {
        return saveRecursive( entity, new HashSet<>());
    }


    // save simple solo una entidad y que no tenga relaciones
    public T save(T entity) {

        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Long id = (Long) idField.get(entity);
            if (id == null) {
                id = idGenerator.incrementAndGet()+(Long) idField.get(entity);
                idField.set(entity, id);
            }
            data.put(id, entity);
            System.out.println(entity.getClass().getSimpleName() + " guardado con id: " + id);

        } catch (Exception e) {
            e.printStackTrace();
        }

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

