import entidades.*;
import repositorioMem.Repositorio;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Main {
    public static void main(String[] args) {

        // cr un Pais
        Pais pais1 = Pais.builder().
                id(01L).
                nombre("Argentina").
                build();

        // cr Provincia y relacionamos con su pais
        Provincia provincia1 = Provincia.builder().
                id(201L).
                nombre("Buenos Aires").
                pais(pais1).
                build();

        Provincia provincia2 = Provincia.builder().
                id(202L).
                nombre("Cordoba").
                pais(pais1).
                build();

        // cr Localidades y relacionamos con su Provincia
        Localidad localidad1= Localidad.builder().
                id(300001L).
                nombre("CaBa").
                provincia(provincia1).
                build();

        Localidad localidad2= Localidad.builder().
                id(3000002L).
                nombre("La Plata").
                provincia(provincia1).
                build();

        Localidad localidad3= Localidad.builder().
                id(3000003L).
                nombre("Cordoba Capital").
                provincia(provincia2).
                build();

        Localidad localidad4= Localidad.builder().
                id(3000004L).
                nombre("Villa Carlos Paz").
                provincia(provincia2).
                build();


        // cr Domicilio y relacionamos con su Localidad
        Domicilio domicilio1 = Domicilio.builder().id(41L).
                calle("San Juan ").
                num(233).piso(0).numDpto(3).
                codPost(0033).localidad(localidad1).
                build();

        Domicilio domicilio2 = Domicilio.builder().id(42L).
                calle("Cr. Bilielli").
                num(335).piso(0).numDpto(1).
                codPost(0023).localidad(localidad2).
                build();

        Domicilio domicilio3 = Domicilio.builder().id(43L).
                calle("Av. Libertador Salvador Jr").
                num(224).piso(0).numDpto(4).
                codPost(5200).localidad(localidad3).
                build();

        Domicilio domicilio4 = Domicilio.builder().id(44L).
                calle("Belgrano").
                num(524).piso(0).numDpto(0).
                codPost(5204).localidad(localidad4).
                build();


        // Creacion de Sucursales
        // en Caba
        Sucursal sucursal1= Sucursal.builder().
                id(5001L).
                nombre("Ford Agencia Centro CABA").
                esCasaMatriz(true).
                horarioApertura(LocalTime.of(9,00)).
                horarioCierre(LocalTime.of(21,30)).
                domicilio(domicilio1).
                build();

        // en la Plata
        Sucursal sucursal2= Sucursal.builder().
                id(5002L).
                nombre("Ford Agencia Centro La PLta").
                esCasaMatriz(false).
                horarioApertura(LocalTime.of(9,00)).
                horarioCierre(LocalTime.of(21,00)).
                domicilio(domicilio2).
                build();

        // en Cordoba Cap
        Sucursal sucursal3= Sucursal.builder().
                id(5003L).
                nombre("Exomindset Sucursal Oeste Crba Cap").
                esCasaMatriz(true).
                horarioApertura(LocalTime.of(14,00)).
                horarioCierre(LocalTime.of(23,30)).
                domicilio(domicilio3).
                build();

        // en Villa Carloz P
        Sucursal sucursal4= Sucursal.builder().
                id(5004L).
                nombre("Exomindset Sucursal Centro Villa Carl P.").
                esCasaMatriz(true).
                horarioApertura(LocalTime.of(14,00)).
                horarioCierre(LocalTime.of(23,30)).
                domicilio(domicilio3).
                build();

        // Creacion de Empresas

        Empresa empresa1 = Empresa.builder().
                id(60001L).
                nombre("Ford").
                razonSocial("Ford Motor Company").
                cuit(121323).
                sucursales(new HashSet<>(Arrays.asList(sucursal1, sucursal2))). //seteamos sus sucursales ->err (java.util.Set<Sucursal>) Arrays.asList(sucursal1,sucursal2)
                 build();                                                                        // ok Stream.of(sucursal1, sucursal2).collect(Collectors.toSet());


        Empresa empresa2 = Empresa.builder().
                id(60002L).
                nombre("Exomindset").
                razonSocial("EXO MINDSET S.A.S").
                cuit(123333).
                sucursales(new HashSet<>(Set.of(sucursal3,sucursal4))).
                build();

        // Setear Bidireccionalidad

        // asignar a sucursales su empresa

        sucursal1.setEmpresa(empresa1);
        sucursal2.setEmpresa(empresa1);

        sucursal3.setEmpresa(empresa2);
        sucursal4.setEmpresa(empresa2);

        // Domicilios a Localidades
        localidad1.setDomicilios(new HashSet<>(Set.of(domicilio1)));
        localidad2.setDomicilios(new HashSet<>(Set.of(domicilio2)));
        localidad3.setDomicilios(new HashSet<>(Set.of(domicilio3)));
        localidad4.setDomicilios(new HashSet<>(Set.of(domicilio4)));

        // Localidades a Provincias
        provincia1.setLocalidades(new HashSet<>(Set.of(localidad1,localidad2)));
        provincia2.setLocalidades(new HashSet<>(Set.of(localidad3,localidad4)));

        // Provincias a Paises
        pais1.setProvincias(new HashSet<>(Set.of(provincia1,provincia2)));


        //Codigo Pruebas de Aplicacion

        // creamos el repositorio
        Repositorio <Empresa> repositorio1 =Repositorio.getRepositorio(Empresa.class);



        // salvar las empresas en el Repositorio (En Cascada -> todas las entidades relacionadas).
        repositorio1.saveRecursive(empresa1);
        repositorio1.saveRecursive(empresa2);

        // Mostrar empresas
        System.out.println("\nMostrando todas las empreasas guardadas (cascada)");

        repositorio1.findAll().forEach(System.out::println); // mostramos cada emp y sus asociacion



        //Buscar empresa por ID -> Usamos optional (el obj puede estar o no -> null)
        Optional<Empresa> empBuscada = repositorio1.findById(60001L).map(e -> (Empresa) e);
            System.out.println("\nExiste empresa con id 60001L");
        // imprime pq existe esa id .

        Optional<Empresa> empBuscada1 = repositorio1.findById(600071L);
        if (empBuscada1.isEmpty()) {
            System.out.println("\nNo existe empresa"); // imprime pq no existe esa id .
        }

        // Buscar empresa por Nombre

        repositorio1.genericFindByField("nombre","Exomindset").forEach(emp -> System.out.println("\nEmpresa encontrada de nombre :" + emp.getNombre()));



        // Actualizar datos de empresa buscada por id
        // cambiar el cuit de la empresa Ford

        Empresa empUpdate = Empresa.builder().
                id(60001L).
                nombre("Ford Ldta").
                razonSocial("Ford Motor Company").
                cuit(1213243). // actualizamos cuit
                sucursales(Stream.of(sucursal1, sucursal2).collect(Collectors.toSet())). //seteamos sus sucursales
                        build();

        Optional<Empresa> update = repositorio1.genericUpdate(60001L, empUpdate);
        System.out.println("\nDatos de Empresa Actualizada : Empresa -> " + update);



        // Eliminar empresa por ID
        repositorio1.genericDelete(60002L);
        Optional<Empresa> empresaEliminada = repositorio1.findById(1L);
        if (empresaEliminada.isEmpty()) {
            System.out.println("\nLa empresa con id 60002 ha sido eliminada existosamente");
        }


        System.out.println("\nMostrando todas las empreasas guardadas (simple) / despues de eliminar");
        repositorio1.findAll().forEach(System.out::println);


    }
}
