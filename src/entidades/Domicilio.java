package entidades;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Builder


public class Domicilio {
    private Long id;
    private String calle;
    private Integer num;
    private Integer codPost;
    private Integer piso;
    private Integer numDpto;

    private Localidad localidad;

}
