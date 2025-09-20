package entidades;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString(exclude = "domicilios")
@Builder

public class Localidad {
    private Long id;
    private String nombre;


    private Set<Domicilio> domicilios = new HashSet<>();
    private Provincia provincia;


}
