package entidades;

import lombok.*;

import java.util.HashSet;
import java.util.Set;


@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Builder

public class Empresa {
    private Long id;
    private String nombre;
    private String razonSocial;
    private Integer cuit;
    private String logo;

    private Set<Sucursal> sucursales = new HashSet<>();

//
//    public void setId(Long id) {
//        this.id = id;
//    }
}
