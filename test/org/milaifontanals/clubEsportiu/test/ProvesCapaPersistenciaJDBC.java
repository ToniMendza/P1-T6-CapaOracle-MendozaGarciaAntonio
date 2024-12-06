/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.milaifontanals.clubEsportiu.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.milaifontanals.clubEsportiu.model.Equip;
import org.milaifontanals.clubEsportiu.model.Jugador;
import org.milaifontanals.clubEsportiu.model.Temporada;
import org.milaifontanals.clubEsportiu.persistencia.*;
import org.milaifontanals.clubEsportiu.jdbc.GestorBDClubEsportiuJdbc;
import org.milaifontanals.clubEsportiu.model.Categoria;
import org.milaifontanals.clubEsportiu.model.ExceptionClub;
import org.milaifontanals.clubEsportiu.model.Membre;
import org.milaifontanals.clubEsportiu.model.Usuari;

/**
 *
 * @author isard
 */
public class ProvesCapaPersistenciaJDBC {

    private static GestorBDClubEsportiuJdbc conBD;

    public static void main(String[] args) throws ParseException, GestorBDClubEsportiuException {

        try {
            //1.Connexio
            conBD = new GestorBDClubEsportiuJdbc();
            System.out.println("Capa de persistència creada");
            System.out.println("");
        } catch (GestorBDClubEsportiuException ex) {
            ex.printStackTrace();
            System.out.println("Problema en crear capa de persistència:");
            System.out.println(ex.getMessage());
            System.out.println("Avortem programa");
            return;
        }

        //2.Obtenir tots els equips
        try {
            ArrayList<Equip> ll;
            ll = (ArrayList<Equip>) conBD.obtenirEquips();
            if (ll.isEmpty()) {
                System.out.println("No hi ha cap equip");
            } else {
                System.out.println("Llistat de equips:");
                for (Equip equip : ll) {
                    System.out.println(equip.toString());
                }
            }

        } catch (GestorBDClubEsportiuException ex) {
            System.out.println("\tError: " + ex.getMessage());
        }

        //3.Obtenir un equip
        try {
            Equip e = conBD.obtenirUnEquip("A");
            System.out.println(e.getNom());
            //Ejemplo de equipo que no existe
//            Equip e = conBD.obtenirUnEquip("Z");
//            System.out.println(e.getNom());
//            conBD.modificarEquip(e);
        } catch (GestorBDClubEsportiuException ex) {
            System.out.println("\tError: " + ex.getMessage());
        }
        System.out.println("");

        //4.Obtenir llista jugadors
        try {
            List<Jugador> ll;
            ll = conBD.obtenirLlistaJugadors(1);
            if (ll.isEmpty()) {
                System.out.println("No hi ha cap jugador");
            } else {
                System.out.println("Llistat de jugadors:");
                for (Jugador jugador : ll) {
                    System.out.println(jugador.toString());
                }
            }

        } catch (GestorBDClubEsportiuException ex) {
            System.out.println("\tError: " + ex.getMessage());
        }

        //5.Obtenir Un usuari
        //Preguntar como hacer lo del hash
        try {
            String pass = "123456";

//            String passHash = encryptThisString(pass);
            Usuari usu = conBD.obtenirUsuari("entrenador_jordi", "jordi123");

            System.out.println("L'usuari es:" + usu);
        } catch (GestorBDClubEsportiuException ex) {
            Logger.getLogger(ProvesCapaPersistenciaJDBC.class.getName()).log(Level.SEVERE, null, ex);
        }

        //4.Obtenir llista jugadors
        try {
            //Depende que jugador no funciona, como Laia por ejemplo
            Jugador j = conBD.filtrarJugadorPelNom("Carla");

            System.out.println(j);

        } catch (GestorBDClubEsportiuException ex) {
            System.out.println("\tError: " + ex.getMessage());
        }

        //ObtenirTemporades 
        try {
            ArrayList<Temporada> llTemp = (ArrayList<Temporada>) conBD.obtenirTemporades();
            System.out.println("Aquestes son totes les temporades");
            for (Temporada temp : llTemp) {
                System.out.println(temp);
            }
            //Ejemplo de equipo que no existe
//            Equip e=conBD.obtenirUnEquip("Z");
//            System.out.println(e.getNom());
        } catch (GestorBDClubEsportiuException ex) {
            Logger.getLogger(ProvesCapaPersistenciaJDBC.class.getName()).log(Level.SEVERE, null, ex);
        }

        //ObtenirTemporada
        try {
            Temporada t = conBD.obtenirUnaTemporada(2024);

            System.out.println(t.getAny_temp());
        } catch (GestorBDClubEsportiuException ex) {
            Logger.getLogger(ProvesCapaPersistenciaJDBC.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        
        // 4. Proves de mètode afegirJugador
try {
    // Crear la fecha directamente
    LocalDate fechaNacimiento = LocalDate.of(2012, 5, 15);
    
    Jugador j = new Jugador("Lorena", "Martinez Santano", 'D', fechaNacimiento, "59467788Z", "ES7600859100051234567810", 2026, "Carrer Argentina 42, Barcelona", "C:\\Users\\isard\\Desktop\\FOTOS-bd\\pexels-danxavier-1121796.jpg");
    System.out.println("Intent d'afegir el jugador");
    conBD.afegirJugador(j);
//    conBD.confirmarCanvis();
    System.out.println("Jugador afegit");
} catch (GestorBDClubEsportiuException ex) {
    System.out.println("\tError: " + ex.getMessage());
}
        System.out.println("");
        
        
//        // 5. Proves de mètode afegirEquip
        try {
//            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yy");
            // Parsear la cadena a Date
            Equip e = null;
//            Date fechaNacimiento = formato.parse("15/05/12");
            e = new Equip("G", 'M', 6, 2025);
            System.out.println("Intent d'afegir el equip");
            conBD.afegirEquip(e);
//            conBD.confirmarCanvis();
            System.out.println("Equip afegit");
        } catch (GestorBDClubEsportiuException ex) {
            System.out.println("\tError: " + ex.getMessage());
        }
        System.out.println("");

        
        
        try {
//            "Titular",,4
            Membre membre = new Membre("CONVIDAT", 6, 14);
            System.out.println("Intent d'afegir membre");
            conBD.afegirMembre(membre);
//            conBD.confirmarCanvis();
            System.out.println("Membre afegit");
            System.out.println(membre);
        } catch (GestorBDClubEsportiuException | ExceptionClub ex) {
            System.out.println("\tError: " + ex.getMessage());
        }
        System.out.println("");

        //ObtenirCategories
        try {
            List<Categoria> cat;
            cat = conBD.obtenirCategories(" ");

            System.out.println(cat.toString());

        } catch (GestorBDClubEsportiuException ex) {
            System.out.println("\tError: " + ex.getMessage());
        }

        //Modificar Jugador
        try {
            // Crear la fecha directamente
            LocalDate fechaNacimiento = LocalDate.of(2012, 5, 15);
            

            Jugador j = new Jugador(1, "Lurdes", "Martinez Santano", 'D', fechaNacimiento, "59402788Z", "ES7612859100051234567810", 2024, "Carrer Argentina 42, Barcelona", "C:\\Users\\isard\\Desktop\\FOTOS-bd\\pexels-danxavier-1121796.jpg");
            System.out.println("Intent de modicar el jugador");
            conBD.modificarJugador(j);
//    conBD.confirmarCanvis();
            System.out.println("Jugador modificat");
        } catch (GestorBDClubEsportiuException | ExceptionClub ex) {
            System.out.println("\tError: " + ex.getMessage());
        }
//
//        obtenirUsuari(String nom, String login)
        //FiltreJugadorCategoria
        LocalDate fechaLocal = LocalDate.of(2007, 4, 3);
        String nif="56789012I";
        // Convertir a java.sql.Date si es necesario
        java.sql.Date fechaSql = java.sql.Date.valueOf(fechaLocal);
        List<Jugador> jugadors = conBD.filtreJugadorCatDataNomNif(5, fechaSql,"Carla",nif);
        jugadors.forEach(j -> System.out.println("Jugadors : " + j.getNom() + "Data naix: " + j.getDataNaixement()+ "Nom: " + j.getNom()));

    }
}
