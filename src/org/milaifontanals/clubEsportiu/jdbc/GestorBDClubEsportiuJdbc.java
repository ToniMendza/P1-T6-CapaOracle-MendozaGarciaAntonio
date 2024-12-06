/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.milaifontanals.clubEsportiu.jdbc;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.milaifontanals.clubEsportiu.model.Categoria;

import org.milaifontanals.clubEsportiu.model.Equip;
import org.milaifontanals.clubEsportiu.model.ExceptionClub;
import org.milaifontanals.clubEsportiu.model.Jugador;
import org.milaifontanals.clubEsportiu.model.Membre;
import org.milaifontanals.clubEsportiu.model.Temporada;
import org.milaifontanals.clubEsportiu.model.Usuari;
import org.milaifontanals.clubEsportiu.persistencia.*;

/**
 *
 * @author antme
 */
public class GestorBDClubEsportiuJdbc implements IGestorBDClubEsportiu {

    /*
     * Aquest objecte és el que ha de mantenir la connexió amb el SGBD Es crea
     * en el constructor d'aquesta classe i manté la connexió fins que el
     * programador decideix tancar la connexió amb el mètode tancarCapa
     */
    private Connection conn;

    /**
     * Sentències preparades que es crearan només 1 vegada i s'utilitzaran
     * tantes vegades com siguin necessàries. En aquest programa es creen la
     * primera vegada que es necessiten i encara no han estat creades. També es
     * podrien crear al principi del programa, una vegada establerta la
     * connexió.
     */
    private PreparedStatement psDelListEquip;
    private PreparedStatement psUpdateEquip;
    private PreparedStatement psInsertEquip;
    private PreparedStatement psDelJugador;
    private PreparedStatement psDelListJugador;
    private PreparedStatement psUpdateJugador;
    private PreparedStatement psInsertJugador;
    private PreparedStatement psDelMembre;
    private PreparedStatement psDelListMembre;
    private PreparedStatement psUpdateMembre;
    private PreparedStatement psInsertMembre;
    private PreparedStatement psInsertTemporada;

    public GestorBDClubEsportiuJdbc() throws GestorBDClubEsportiuException {
        String nomFitxer = "clubEsportiu.properties";

        try {
            Properties props = new Properties();
            props.load(new FileReader(nomFitxer));
            String[] claus = {"url", "user", "password"};
            String[] valors = new String[3];
//En este caso(String[] valors):
//
//valors es una referencia a un array de String, pero aún no apunta a ninguna instancia de array.
//Intentar acceder o asignar elementos en valors antes de inicializarlo lanzará una excepción NullPointerException.
            for (int i = 0; i < claus.length; i++) {
                valors[i] = props.getProperty(claus[i]);
                if (valors[i] == null || valors[i].isEmpty()) {
                    throw new GestorBDClubEsportiuException("L'arxiu " + nomFitxer + " no troba la clau " + claus[i]);
                }
            }
            conn = DriverManager.getConnection(valors[0], valors[1], valors[2]);
            conn.setAutoCommit(false);
        } catch (IOException ex) {
            throw new GestorBDClubEsportiuException("Problemes en recuperar l'arxiu de configuració " + nomFitxer, ex);
        } catch (SQLException ex) {
            throw new GestorBDClubEsportiuException("No es pot establir la connexió.", ex);
        }
    }

    /**
     * Tanca la connexió
     *
     * @throws
     * org.milaifontanals.clubEsportiu.persistencia.GestorBDClubEsportiuException;
     */
    @Override
    public void tancarCapa() throws GestorBDClubEsportiuException {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                throw new GestorBDClubEsportiuException("Error en fer rollback final.", ex);
            }
            try {
                conn.close();
            } catch (SQLException ex) {
                throw new GestorBDClubEsportiuException("Error en tancar la connexió.\n", ex);
            }
        }
    }

    @Override
    public void recuperarContrasenya() throws GestorBDClubEsportiuException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    //
    @Override
    public List<Equip> obtenirEquips() throws GestorBDClubEsportiuException {
        List<Equip> llEquips = new ArrayList<>();
        Statement st = null;

        try {
            st = conn.createStatement();
            ResultSet rs = st.executeQuery("select * from equip");

            while (rs.next()) {
                llEquips.add(new Equip(rs.getInt("id"), rs.getString("nom"), rs.getString("tipus").charAt(0), rs.getInt("id_categoria"), rs.getInt("id_any")));
            }
        } catch (SQLException ex) {
            throw new GestorBDClubEsportiuException("Error en intentar recuperar la llista de Equips.", ex);
        } catch (ExceptionClub ex) {
            Logger.getLogger(GestorBDClubEsportiuJdbc.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    throw new GestorBDClubEsportiuException("Error en intentar tancar la sentència que ha recuperat la llista de productes.", ex);
                }
            }
        }
        return llEquips;

    }

    //
    @Override
    public Equip obtenirUnEquip(String nom) throws GestorBDClubEsportiuException {
        Equip e = null;
        PreparedStatement prst = null;

        try {

            prst = conn.prepareStatement("SELECT * FROM equip WHERE UPPER(nom) LIKE UPPER(?)");

            prst.setString(1, "%" + nom + "%");

            ResultSet rs = prst.executeQuery();

            if (rs.next()) {
                e = new Equip(rs.getInt("id"), rs.getString("nom"), rs.getString("tipus").charAt(0), rs.getInt("id_categoria"), rs.getInt("id_any"));
            } else {
                throw new GestorBDClubEsportiuException("No hi ha cap equp amb aquest nom");

            }
        } catch (SQLException ex) {
            throw new GestorBDClubEsportiuException("Error en intentar recuperar la llista de Equips.", ex);
        } catch (ExceptionClub ex) {
            Logger.getLogger(GestorBDClubEsportiuJdbc.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (prst != null) {
                try {
                    prst.close();
                } catch (SQLException ex) {
                    throw new GestorBDClubEsportiuException("Error en intentar tancar la sentència que ha recuperat la llista de productes.", ex);
                }
            }
        }
        return e;
    }

    //
    @Override
    public List<Jugador> obtenirLlistaJugadors(int number) throws GestorBDClubEsportiuException {
        List<Jugador> jugadors = new ArrayList<>();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            if (number == 1) {
                rs = st.executeQuery("SELECT * FROM jugadors order by COGNOMS");
            } else {
                rs = st.executeQuery("SELECT * FROM jugadors order by DATA_NAIX");
            }

            while (rs.next()) {
                jugadors.add(new Jugador(rs.getInt("id"), rs.getString("nom"), rs.getString("cognoms"), rs.getString("sexe").charAt(0), rs.getDate("data_naix").toLocalDate(), rs.getString("idlegal"), rs.getString("iban"), rs.getInt("any_fi_revisio"), rs.getString("adreca"), rs.getString("foto")));
            }
        } catch (SQLException | ExceptionClub ex) {
            throw new GestorBDClubEsportiuException("Error en intentar recuperar la llista de jugadors", ex);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    throw new GestorBDClubEsportiuException("Error en intentar tancar la sentència que ha recuperat la llista de productes.", ex);
                }
            }
        }
        return jugadors;
    }
//    @Override
//    public List<Jugador> filtreJugadorCategoriaData(String categoria, java.util.Date dataNaix) throws GestorBDClubEsportiuException {
//        List<Jugador> jugadors = new ArrayList<>();
//        PreparedStatement st = null;
//        ResultSet rs = null;
//        List<Categoria> llCat = this.obtenirCategories(categoria);
//        try {
//
//            if (llCat.size() > 1) {
//                st = conn.prepareStatement("SELECT * FROM jugadors");
//            } else {
//
//                st = conn.prepareStatement("SELECT * FROM jugadors where ((sysdate-data_naix)/365) > ? and ((sysdate-data_naix)/365)< ?");
//                st.setInt(1, llCat.get(0).getEdatMin());
//                st.setInt(2, llCat.get(0).getEdatMin());
//            }
//            rs = st.executeQuery();
//            while (rs.next()) {
//                jugadors.add(new Jugador(rs.getInt("id"), rs.getString("nom"), rs.getString("cognoms"), rs.getString("sexe").charAt(0), rs.getDate("data_naix"), rs.getString("idlegal"), rs.getString("iban"), rs.getInt("any_fi_revisio"), rs.getString("adreca"), rs.getString("foto")));
//            }
//        } catch (SQLException | ExceptionClub ex) {
//            throw new GestorBDClubEsportiuException("Error en intentar recuperar la llista de jugadors", ex);
//        } finally {
//            if (st != null) {
//                try {
//                    st.close();
//                } catch (SQLException ex) {
//                    throw new GestorBDClubEsportiuException("Error en intentar tancar la sentència que ha recuperat la llista de productes.", ex);
//                }
//            }
//        }
//        return jugadors;
//    }

//Alternativa consulta sql
/*select j.* from membre m join equip e on m.id_equip=e.id join jugador j on m.id_jugador=j.id where 
e.id_categoria=(select id from categoria where nom like('SENIOR')) and e.id_any=2024 and j.data_naix='22/09/98' order by m.id_equip;*/
//    @Override
//public List<Jugador> filtreJugadorCategoriaData(String categoria, Date dNaix) throws GestorBDClubEsportiuException {
//    List<Jugador> jugadors = new ArrayList<>();
//    PreparedStatement st = null;
//    ResultSet rs = null;
//    String query;
//
//    try {
//        // Obtener las categorías para verificar si hay más de una
//        List<Categoria> llCat = this.obtenirCategories(categoria);
//
//        if (llCat.size() > 1) {
//            // Si hay más de una categoría, seleccionamos todos los jugadores ordenados por nombre
//            query = "SELECT * FROM jugador ORDER BY nom";
//            st = conn.prepareStatement(query);
//        } else if (dNaix != null) {
//            // Si hay una sola categoría y se proporciona fecha de nacimiento
//            query = "SELECT j.* " +
//                    "FROM membre m " +
//                    "JOIN equip e ON m.id_equip = e.id " +
//                    "JOIN jugador j ON m.id_jugador = j.id " +
//                    "WHERE e.id_categoria = (SELECT id FROM categoria WHERE UPPER(nom) = UPPER(?)) " +
//                    "AND e.id_any = 2024 " +
//                    "AND j.data_naix = ? " +
//                    "ORDER BY m.id_equip";
//            st = conn.prepareStatement(query);
//            st.setString(1, categoria);
//            st.setDate(2, new java.sql.Date(dNaix.getTime()));
//        } else {
//            // Si no hay fecha de nacimiento, pero solo una categoría
//            query = "SELECT j.* " +
//                    "FROM membre m " +
//                    "JOIN equip e ON m.id_equip = e.id " +
//                    "JOIN jugador j ON m.id_jugador = j.id " +
//                    "WHERE e.id_categoria = (SELECT id FROM categoria WHERE UPPER(nom) = UPPER(?)) " +
//                    "AND e.id_any = 2024 " +
//                    "ORDER BY m.id_equip";
//            st = conn.prepareStatement(query);
//            st.setString(1, categoria);
//        }
//
//        // Ejecutar la consulta SQL
//        rs = st.executeQuery();
//
//        // Recorrer el resultado
//        while (rs.next()) {
//            // Construir el objeto Jugador con los datos de la base de datos
//            Jugador jugador = new Jugador(
//                    rs.getInt("id"),
//                    rs.getString("nom"),
//                    rs.getString("cognoms"),
//                    rs.getString("sexe").charAt(0),
//                    rs.getDate("data_naix"),
//                    rs.getString("idlegal"),
//                    rs.getString("iban"),
//                    rs.getInt("any_fi_revisio"),
//                    rs.getString("adreca"),
//                    rs.getString("foto")
//            );
//            jugadors.add(jugador);
//        }
//    } catch (SQLException ex) {
//        throw new GestorBDClubEsportiuException("Error al intentar recuperar la lista de jugadores", ex);
//    }   catch (ExceptionClub ex) {
//            Logger.getLogger(GestorBDClubEsportiuJdbc.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//        // Cerrar los recursos
//        if (rs != null) {
//            try {
//                rs.close();
//            } catch (SQLException ex) {
//                throw new GestorBDClubEsportiuException("Error al cerrar el ResultSet", ex);
//            }
//        }
//        if (st != null) {
//            try {
//                st.close();
//            } catch (SQLException ex) {
//                throw new GestorBDClubEsportiuException("Error al cerrar el PreparedStatement", ex);
//            }
//        }
//    }
//    return jugadors;
//}
    
    
    
       
//    @Override
//public List<Jugador> filtreJugadorCatDataNomNif(int categoriaId, Date dNaix,String nom,String nif) throws GestorBDClubEsportiuException {
//    List<Jugador> jugadors = new ArrayList<>();
//    StringBuilder queryBuilder = new StringBuilder();
//    
//    //Query para devolver tambien la categoria del jugador junto con la info del jugador y el nombre del equip  y el tipo:
//    //SELECT j.*,e.nom,e.tipus,cat.nom FROM membre m JOIN equip e ON m.id_equip = e.id JOIN jugador j ON m.id_jugador = j.id join categoria cat on e.id_categoria=cat.id WHERE e.id_any = 2024 and j.nom like('Carla');
//    queryBuilder.append("SELECT distinct j.* FROM membre m JOIN equip e ON m.id_equip = e.id JOIN jugador j ON m.id_jugador = j.id WHERE e.id_any = 2024 ");
//
//    // Añadimos filtros dinámicos según los parámetros
//    if (categoriaId != 0) {
//        queryBuilder.append("AND e.id_categoria = ? ");
//    }
//    if (dNaix != null) {
//        queryBuilder.append("AND j.data_naix = ? ");
//    }
//    if(nom!=null){
//        queryBuilder.append("AND upper(j.nom) like upper(?) ");
//    }
//    if(nom!=null){
//        queryBuilder.append("AND upper(j.IDLEGAL) like upper(?) ");
//    }
//    
//    //no se puede ordenar porque sino no funcionara
////    queryBuilder.append("ORDER BY m.id_equip");
//
//    try (
//        PreparedStatement st = conn.prepareStatement(queryBuilder.toString())
//    ) {
//        // Configuración dinámica de parámetros
//        int paramIndex = 1;
//        if (categoriaId != 0) {
//            st.setInt(paramIndex++, categoriaId);
//        }
//        if (dNaix != null) {
//            st.setDate(paramIndex++, dNaix);
//        }
//        if(nom!= null){
//            st.setString(paramIndex++, nom);
//        }
//        if(nif!= null){
//            st.setString(paramIndex, nif);
//        }
//
//        try (ResultSet rs = st.executeQuery()) {
//            while (rs.next()) {
//                // Crear objetos Jugador y añadirlos a la lista
//                jugadors.add(new Jugador(
//                    rs.getInt("id"),
//                    rs.getString("nom"),
//                    rs.getString("cognoms"),
//                    rs.getString("sexe").charAt(0),
//                    rs.getDate("data_naix").toLocalDate(),
//                    rs.getString("idlegal"),
//                    rs.getString("iban"),
//                    rs.getInt("any_fi_revisio"),
//                    rs.getString("adreca"),
//                    rs.getString("foto")
//                ));
//            }
//        } catch (ExceptionClub ex) {
//            Logger.getLogger(GestorBDClubEsportiuJdbc.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    } catch (SQLException ex) {
//        throw new GestorBDClubEsportiuException("Error al recuperar la lista de jugadores", ex);
//    }
//
//    return jugadors;
//}

@Override
public List<Jugador> filtreJugadorCatDataNomNif(int categoriaId, Date dNaix,String nom,String nif) throws GestorBDClubEsportiuException {
    List<Jugador> jugadors = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();
    
    //Query para devolver tambien la categoria del jugador junto con la info del jugador y el nombre del equip  y el tipo:
    //SELECT j.*,e.nom,e.tipus,cat.nom FROM membre m JOIN equip e ON m.id_equip = e.id JOIN jugador j ON m.id_jugador = j.id join categoria cat on e.id_categoria=cat.id WHERE e.id_any = 2024 and j.nom like('Carla');
    queryBuilder.append("SELECT J.*,C.NOM, c.id FROM JUGADOR J JOIN CATEGORIA C ON (2024 - EXTRACT(YEAR FROM J.DATA_NAIX)) BETWEEN C.EDAT_MIN AND C.EDAT_MAX ");

    // Añadimos filtros dinámicos según los parámetros
    if (categoriaId != 0) {
        queryBuilder.append(" AND c.id = ? ");
    }
    if (dNaix != null) {
        queryBuilder.append(" AND j.data_naix = ? ");
    }
    if(nom!=null){
        queryBuilder.append(" AND upper(j.nom) like upper(?) ");
    }
    if(nom!=null){
        queryBuilder.append(" AND upper(j.IDLEGAL) like upper(?) ");
    }
    
    //no se puede ordenar porque sino no funcionara
//    queryBuilder.append("ORDER BY m.id_equip");

    try (
        PreparedStatement st = conn.prepareStatement(queryBuilder.toString())
    ) {
        // Configuración dinámica de parámetros
        int paramIndex = 1;
        if (categoriaId != 0) {
            st.setInt(paramIndex++, categoriaId);
        }
        if (dNaix != null) {
            st.setDate(paramIndex++, dNaix);
        }
        if(nom!= null){
            st.setString(paramIndex++, nom);
        }
        if(nif!= null){
            st.setString(paramIndex, nif);
        }

        try (ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                // Crear objetos Jugador y añadirlos a la lista
                jugadors.add(new Jugador(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("cognoms"),
                    rs.getString("sexe").charAt(0),
                    rs.getDate("data_naix").toLocalDate(),
                    rs.getString("idlegal"),
                    rs.getString("iban"),
                    rs.getInt("any_fi_revisio"),
                    rs.getString("adreca"),
                    rs.getString("foto")
                ));
            }
        } catch (ExceptionClub ex) {
            Logger.getLogger(GestorBDClubEsportiuJdbc.class.getName()).log(Level.SEVERE, null, ex);
        }
    } catch (SQLException ex) {
        throw new GestorBDClubEsportiuException("Error al recuperar la lista de jugadores", ex);
    }

    return jugadors;
}


//private String obtenirCategoriaPerEdat(int edat){
//            PreparedStatement prst = null;
//
//        try {
//
//            prst = conn.prepareStatement("SELECT * FROM equip WHERE UPPER(nom) LIKE UPPER(?)");
//
//            prst.setString(1, "%" + nom + "%");
//
//            ResultSet rs = prst.executeQuery();
//
//            if (rs.next()) {
//                e = new Equip(rs.getInt("id"), rs.getString("nom"), rs.getString("tipus").charAt(0), rs.getInt("id_categoria"), rs.getInt("id_any"));
//            } else {
//                throw new GestorBDClubEsportiuException("No hi ha cap equp amb aquest nom");
//
//            }
//        } catch (SQLException ex) {
//            throw new GestorBDClubEsportiuException("Error en intentar recuperar la llista de Equips.", ex);
//        } catch (ExceptionClub ex) {
//            Logger.getLogger(GestorBDClubEsportiuJdbc.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            if (prst != null) {
//                try {
//                    prst.close();
//                } catch (SQLException ex) {
//                    throw new GestorBDClubEsportiuException("Error en intentar tancar la sentència que ha recuperat la llista de productes.", ex);
//                }
//            }
//        }
//        return e;
//}

//public List<Jugador> filtreJugadorCatDataNomNifDos(int categoriaId, Date dNaix,String nom,String nif,int Temporada) throws GestorBDClubEsportiuException {
//    List<Jugador> jugadors = this.obtenirLlistaJugadors(2);
//    for (Jugador jugador : jugadors) {
//        jugador.setEdat( Temporada-jugador.getDataNaixement().getYear());
//    }
//    StringBuilder queryBuilder = new StringBuilder();
//    
//    //Query para devolver tambien la categoria del jugador junto con la info del jugador y el nombre del equip  y el tipo:
//    //SELECT j.*,e.nom,e.tipus,cat.nom FROM membre m JOIN equip e ON m.id_equip = e.id JOIN jugador j ON m.id_jugador = j.id join categoria cat on e.id_categoria=cat.id WHERE e.id_any = 2024 and j.nom like('Carla');
//    queryBuilder.append("SELECT distinct j.* FROM membre m JOIN equip e ON m.id_equip = e.id JOIN jugador j ON m.id_jugador = j.id WHERE e.id_any = 2024 ");
//
//    // Añadimos filtros dinámicos según los parámetros
//    if (categoriaId != 0) {
//        queryBuilder.append("AND e.id_categoria = ? ");
//    }
//    if (dNaix != null) {
//        queryBuilder.append("AND j.data_naix = ? ");
//    }
//    if(nom!=null){
//        queryBuilder.append("AND upper(j.nom) like upper(?) ");
//    }
//    if(nom!=null){
//        queryBuilder.append("AND upper(j.IDLEGAL) like upper(?) ");
//    }
//    
//    //no se puede ordenar porque sino no funcionara
////    queryBuilder.append("ORDER BY m.id_equip");
//
//    try (
//        PreparedStatement st = conn.prepareStatement(queryBuilder.toString())
//    ) {
//        // Configuración dinámica de parámetros
//        int paramIndex = 1;
//        if (categoriaId != 0) {
//            st.setInt(paramIndex++, categoriaId);
//        }
//        if (dNaix != null) {
//            st.setDate(paramIndex++, dNaix);
//        }
//        if(nom!= null){
//            st.setString(paramIndex++, nom);
//        }
//        if(nif!= null){
//            st.setString(paramIndex, nif);
//        }
//
//        try (ResultSet rs = st.executeQuery()) {
//            while (rs.next()) {
//                // Crear objetos Jugador y añadirlos a la lista
//                jugadors.add(new Jugador(
//                    rs.getInt("id"),
//                    rs.getString("nom"),
//                    rs.getString("cognoms"),
//                    rs.getString("sexe").charAt(0),
//                    rs.getDate("data_naix").toLocalDate(),
//                    rs.getString("idlegal"),
//                    rs.getString("iban"),
//                    rs.getInt("any_fi_revisio"),
//                    rs.getString("adreca"),
//                    rs.getString("foto")
//                ));
//            }
//        } catch (ExceptionClub ex) {
//            Logger.getLogger(GestorBDClubEsportiuJdbc.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    } catch (SQLException ex) {
//        throw new GestorBDClubEsportiuException("Error al recuperar la lista de jugadores", ex);
//    }
//
//    return jugadors;
//}

//     @Override
//    public List<Jugador> filtreJugadorCategoriaData(int categoriaId) throws GestorBDClubEsportiuException {
//        List<Jugador> jugadors = new ArrayList<>();
//        PreparedStatement st = null;
//        ResultSet rs = null;
//        String query;
//        try {
//            
//            if (categoriaId == 0) {
//                // Si hay más de una categoría para esta categoría, obtenemos todos los jugadores
//                query = "SELECT * FROM jugador order by nom";
//                st = conn.prepareStatement(query);
//            } else {
//        query = "SELECT j.* " +
//        "FROM membre m " +
//        "JOIN equip e ON m.id_equip = e.id " +
//        "JOIN jugador j ON m.id_jugador = j.id " +
//        "WHERE e.id_categoria = ? " +
//        "AND e.id_any = 2024 " +
//        "ORDER BY m.id_equip";
//                st = conn.prepareStatement(query);
//
//                st.setInt(1, categoriaId);
//            }
//
//            // Ejecutamos la consulta SQL
//            rs = st.executeQuery();
//
//            // Recorremos el resultado
//            while (rs.next()) {
//                // Obtenemos la información de cada jugador de la base de datos
//                Jugador jugador = new Jugador(
//                        rs.getInt("id"),
//                        rs.getString("nom"),
//                        rs.getString("cognoms"),
//                        rs.getString("sexe").charAt(0),
//                        rs.getDate("data_naix"),
//                        rs.getString("idlegal"),
//                        rs.getString("iban"),
//                        rs.getInt("any_fi_revisio"),
//                        rs.getString("adreca"),
//                        rs.getString("foto")
//                );
//                jugadors.add(jugador);
//            }
//        } catch (SQLException ex) {
//            throw new GestorBDClubEsportiuException("Error en intentar recuperar la lista de jugadores", ex);
//        } catch (ExceptionClub ex) {
//            throw new GestorBDClubEsportiuException("Error desconocido", ex);
//        } finally {
//            // Cerramos la sentencia preparada y el result set
//            if (st != null) {
//                try {
//                    st.close();
//                } catch (SQLException ex) {
//                    throw new GestorBDClubEsportiuException("Error al cerrar la sentencia", ex);
//                }
//            }
//            if (rs != null) {
//                try {
//                    rs.close();
//                } catch (SQLException ex) {
//                    throw new GestorBDClubEsportiuException("Error al cerrar el result set", ex);
//                }
//            }
//        }
//        return jugadors;
//    }
    
    //
    @Override
    public Usuari obtenirUsuari(String nom, String password) throws GestorBDClubEsportiuException {
        Usuari usu = null;
        PreparedStatement prst = null;
        ResultSet rs = null;
        try {
            prst = conn.prepareStatement("select * from usuari where upper(login) like upper(?) and upper(password_usu) like upper(?)");
            prst.setString(1, nom);
            prst.setString(2, convertToSH1(password));

            rs = prst.executeQuery();
            if (rs != null && rs.next()) {
                usu = new Usuari(rs.getString("login"), rs.getString("nom"), rs.getString("PASSWORD_USU"));
            } else {
                throw new GestorBDClubEsportiuException("No hi ha cap usuari amb aquest nom");
            }
        } catch (SQLException ex) {
            throw new GestorBDClubEsportiuException("Error en intentar recuperar l'usuari.", ex);
        } finally {
            if (prst != null) {
                try {
                    prst.close();
                } catch (SQLException ex) {
                    throw new GestorBDClubEsportiuException("Error en intentar tancar la sentència que ha recuperat l'usuari.", ex);
                }
            }
        }
        return usu;
    }
    //Verifica si realmente haces en algun momento busqueda por nombre, sino es asi cambia el metodo para que solo devuelva toda la información
    @Override
    public List<Categoria> obtenirCategories(String nom) throws GestorBDClubEsportiuException {
        List<Categoria> categoria = new ArrayList<>();

        String sql = nom == null || nom.trim().isEmpty()? "SELECT * FROM categoria": "SELECT * FROM categoria WHERE UPPER(nom) LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Si hay un nombre, configurar el parámetro para la búsqueda
            if (nom != null && !nom.trim().isEmpty()) {
                ps.setString(1, "%" + nom.trim().toUpperCase() + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categoria.add(new Categoria(rs.getInt("id"), rs.getString("nom"), rs.getInt("edat_min"), rs.getInt("edat_max")
                    ));
                }
            } catch (ExceptionClub ex) {
                Logger.getLogger(GestorBDClubEsportiuJdbc.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            throw new GestorBDClubEsportiuException("Error en intentar recuperar les categories", ex);
        }
        return categoria;
    }

    @Override
    public List<Jugador> obtenirJugadorsEquip(String nomEquip) throws GestorBDClubEsportiuException {
        List<Jugador> llJugadors = null;
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody

//        PreparedStatement prst=conn.prepareStatement("select * from ")
    }

    @Override
    public Jugador filtrarJugadorPelNom(String nom) throws GestorBDClubEsportiuException {
        Jugador jugador = null;
        PreparedStatement prst = null;
        ResultSet rs = null;
        try {
            prst = conn.prepareStatement("SELECT * FROM jugador WHERE UPPER(nom) LIKE UPPER(?)");
            prst.setString(1, nom.trim());

            rs = prst.executeQuery();
            if (nom == null || nom.trim().isEmpty()) {
                throw new GestorBDClubEsportiuException("El nom del jugador no pot ser nul o buit.");
            } else if (rs != null && rs.next()) {
                jugador = new Jugador(rs.getInt("id"), rs.getString("nom"), rs.getString("cognoms"), rs.getString("sexe").charAt(0), rs.getDate("data_naix").toLocalDate(), rs.getString("idlegal"), rs.getString("iban"), rs.getInt("any_fi_revisio"), rs.getString("adreca"), rs.getString("foto"));
            } else {
                throw new GestorBDClubEsportiuException("No hi ha cap jugador amb aquest nom");
            }
        } catch (SQLException ex) {
            throw new GestorBDClubEsportiuException("Error en intentar recuperar el jugador.", ex);
        } catch (ExceptionClub ex) {
            Logger.getLogger(GestorBDClubEsportiuJdbc.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (prst != null) {
                try {
                    prst.close();
                } catch (SQLException ex) {
                    throw new GestorBDClubEsportiuException("Error en intentar tancar la sentència que ha recuperat la llista de productes.", ex);
                }
            }
        }
        return jugador;
    }
    
    @Override
    public Jugador filtrarJugadorPerId(int id) throws GestorBDClubEsportiuException {
    Jugador jugador = null;
    PreparedStatement prst = null;
    ResultSet rs = null;

    try {
        prst = conn.prepareStatement("SELECT * FROM jugador WHERE id = ?");
        prst.setInt(1, id);

        rs = prst.executeQuery();
        if (rs != null && rs.next()) {
            jugador = new Jugador(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("cognoms"),
                rs.getString("sexe").charAt(0),
                rs.getDate("data_naix").toLocalDate(),
                rs.getString("idlegal"),
                rs.getString("iban"),
                rs.getInt("any_fi_revisio"),
                rs.getString("adreca"),
                rs.getString("foto")
            );
        } else {
            throw new GestorBDClubEsportiuException("No hi ha cap jugador amb aquest ID");
        }
    } catch (SQLException ex) {
        throw new GestorBDClubEsportiuException("Error en intentar recuperar el jugador.", ex);
    } catch (ExceptionClub ex) {
        Logger.getLogger(GestorBDClubEsportiuJdbc.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
        if (prst != null) {
            try {
                prst.close();
            } catch (SQLException ex) {
                throw new GestorBDClubEsportiuException("Error en intentar tancar la sentència que ha recuperat el jugador.", ex);
            }
        }
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ex) {
                throw new GestorBDClubEsportiuException("Error en intentar tancar el resultset.", ex);
            }
        }
    }
    return jugador;
}
//Añadir a interficie
//
//    public Jugador filtrarJugador(String nom) throws GestorBDClubEsportiuException {
//        Jugador jugador = null;
//        PreparedStatement prst = null;
//        ResultSet rs = null;
//        try {
//            prst = conn.prepareStatement("SELECT * FROM jugador WHERE UPPER(nom) LIKE UPPER(?)");
//            prst.setString(1, nom);
//
//            rs = prst.executeQuery();
//            if (rs != null && rs.next()) {
//                jugador = new Jugador(rs.getInt("id"), rs.getString("nom"), rs.getString("cognoms"), rs.getString("sexe").charAt(0), rs.getDate("data_naix").toLocalDate(), rs.getString("idlegal"), rs.getString("iban"), rs.getInt("any_fi_revisio"), rs.getString("adreca"), rs.getString("foto"));
//            } else {
//                throw new GestorBDClubEsportiuException("No hi ha cap jugador amb aquest nom");
//            }
//        } catch (SQLException | ExceptionClub ex) {
//            throw new GestorBDClubEsportiuException("Error en intentar recuperar el jugador.", ex);
//        } finally {
//            if (prst != null) {
//                try {
//                    prst.close();
//                } catch (SQLException ex) {
//                    throw new GestorBDClubEsportiuException("Error en intentar tancar la sentència que ha recuperat la llista de productes.", ex);
//                }
//            }
//        }
//        return jugador;
//    }

    //
    @Override
    public Temporada obtenirUnaTemporada(int any) throws GestorBDClubEsportiuException {
        Temporada temp = null;
        PreparedStatement prst = null;
        ResultSet rs = null;
        try {
            prst = conn.prepareStatement("SELECT * FROM temporada WHERE any_temp=(?)");
            prst.setInt(1, any);

            rs = prst.executeQuery();
//            ,rs.getDate("data_naix")
            if (rs != null && rs.next()) {
                temp = new Temporada(rs.getInt("any_temp"));
            } else {
                throw new GestorBDClubEsportiuException("No hi ha cap temporada amb aquest any");
            }
        } catch (SQLException ex) {
            throw new GestorBDClubEsportiuException("Error en intentar recuperar la temporada", ex);
        } finally {
            if (prst != null) {
                try {
                    prst.close();
                } catch (SQLException ex) {
                    throw new GestorBDClubEsportiuException("Error en intentar tancar la sentència que ha recuperat la llista de productes.", ex);
                }
            }
        }
        return temp;
    }

    //
    @Override
    public List<Temporada> obtenirTemporades() throws GestorBDClubEsportiuException {
        List<Temporada> temp = new ArrayList<>();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery("SELECT * FROM temporada");

            while (rs.next()) {
                temp.add(new Temporada(rs.getInt("any_temp")));
            }
        } catch (SQLException ex) {
            throw new GestorBDClubEsportiuException("Error en intentar recuperar la temporada", ex);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    throw new GestorBDClubEsportiuException("Error en intentar tancar la sentència que ha recuperat la llista de productes.", ex);
                }
            }
        }
        return temp;
    }

    //
    @Override
    public void afegirEquip(Equip e) throws GestorBDClubEsportiuException {
        if (psInsertEquip == null) {
            try {
                psInsertEquip = conn.prepareStatement("INSERT INTO equip(NOM,TIPUS,ID_CATEGORIA,ID_ANY) VALUES (?,?,?,?)");
                //nom,tipus,id_categoria,id_any
            } catch (SQLException ex) {
                throw new GestorBDClubEsportiuException("Error en preparar sentència psInsertEquip", ex);
            }
        }
        try {
            psInsertEquip.setString(1, e.getNom());
            psInsertEquip.setString(2, Character.toString(e.getTipus()));
            psInsertEquip.setInt(3, e.getIdCategoria());
            psInsertEquip.setInt(4, e.getIdAny());
            psInsertEquip.executeUpdate();
        } catch (SQLException ex) {
            throw new GestorBDClubEsportiuException("Error en intentar inserir el equip " + e.getNom(), ex);
        }
    }

    @Override
    public void modificarEquip(Equip e) throws GestorBDClubEsportiuException {
        if (psUpdateEquip == null) {
            try {
                psUpdateEquip = conn.prepareStatement("UPDATE equip SET nom=? , tipus=?, id_categoria=?, id_any=? WHERE id=?");
            } catch (SQLException ex) {
                throw new GestorBDClubEsportiuException("Error en preparar sentència psUpdateEquip", ex);
            }
        }
        try {

            psUpdateEquip.setString(1, e.getNom());
            psUpdateEquip.setString(2, Character.toString(e.getTipus()));
            psUpdateEquip.setInt(3, e.getIdCategoria());
            psUpdateEquip.setInt(4, e.getIdAny());
            psUpdateEquip.executeUpdate();
            int q = psUpdateEquip.executeUpdate();
            if (q == 0) {
                throw new GestorBDClubEsportiuException("Equip no modificat per no existir");
            }
        } catch (SQLException ex) {
            throw new GestorBDClubEsportiuException("Error en intentar modificar l'equip " + e.getNom(), ex);
        }
    }

    @Override
    public void eliminarUnEquip(Equip e) throws GestorBDClubEsportiuException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void eliminarEquips(List<Integer> ll) throws GestorBDClubEsportiuException {
        // ALERTA: El lògic és disposar d'un PreparedStatement amb un paràmetre que sigui la
        // llista de valors a eliminar i usar el mètode setArray definit a JDBC per emplenar-lo.
        // La teoria diu que hauria de ser, suposant PreparedStatement psDelListEquip:
        // psDelListEquip = conn.prepareStatement("DELETE FROM PRODUCTE WHERE prod_num IN (?)");
        // psDelListEquip.setArray(1, conn.createArrayOf("INTEGER", ll.toArray()));
        // PERÒ no tots els SGBD faciliten aquesta funcionalitat (Oracle - MySQL8)
        // o la faciliten amb sintaxis pròpia
        // Per tant, NO usarem PreparedStatement per eliminar d'una sola vegada tots els productes
        // indicats, sinó que ho farem 1 a 1, per garantir funcionalitat en tots els SGBD
        if (psDelListEquip == null) {
            try {
                psDelListEquip = conn.prepareStatement("DELETE FROM equip WHERE id = ?");
            } catch (SQLException ex) {
                throw new GestorBDClubEsportiuException("Error en preparar sentència psDelListEquip", ex);
            }
        }
        Savepoint sp = null;
        try {
            sp = conn.setSavepoint();
            int q = 0;
            for (Integer codi : ll) {
                psDelListEquip.setInt(1, codi);
                q = q + psDelListEquip.executeUpdate();
            }
            if (q < ll.size()) {
                conn.rollback(sp);
                throw new GestorBDClubEsportiuException("No s'ha efectuat l'eliminació per què "
                        + "no s'han trobat tots els equips que es demanava d'eliminar");
            }
        } catch (SQLException ex) {
            if (sp != null) {
                try {
                    conn.rollback(sp);
                } catch (SQLException ex1) {
                }
            }
            throw new GestorBDClubEsportiuException("Error en eliminar els equips de codi indicat", ex);
        }
    }

//psInsertMembre
    @Override
    public void afegirMembre(Membre m) throws GestorBDClubEsportiuException {
        if (psInsertMembre == null) {
            try {
                psInsertMembre = conn.prepareStatement("INSERT INTO(TITULAR_CONVIDAT,ID_EQUIP,ID_JUGADOR) MEMBRE VALUES (?,?,?)");
                //nom,tipus,id_categoria,id_any
            } catch (SQLException ex) {
                throw new GestorBDClubEsportiuException("Error en preparar sentència psInsertMembre", ex);
            }
        }
        try {
            psInsertMembre.setString(1, m.getTitulaConvidat());
            psInsertMembre.setInt(2, m.getIdEquip());
            psInsertMembre.setInt(3, m.getIdJugador());

        } catch (SQLException ex) {
            throw new GestorBDClubEsportiuException("Error en intentar inserir el Membre " + m.getIdEquip(), ex);
        }
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody

    }

    @Override
    public void modificarMembre(Membre m) throws GestorBDClubEsportiuException {
        if (psUpdateMembre == null) {
            try {
                psUpdateMembre = conn.prepareStatement("INSERT INTO MEMBRE VALUES (?,?)");
                //nom,tipus,id_categoria,id_any
            } catch (SQLException ex) {
                throw new GestorBDClubEsportiuException("Error en preparar sentència psInsertMembre", ex);
            }
        }
        try {
            psUpdateMembre.setString(1, m.getTitulaConvidat());
            psUpdateMembre.setInt(2, m.getIdEquip());
            psUpdateMembre.setInt(3, m.getIdJugador());

        } catch (SQLException ex) {
            throw new GestorBDClubEsportiuException("Error en intentar inserir al modificar el membre: " + m.getIdEquip(), ex);
        }
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody

    }

    @Override
    public void eliminarMembre(Membre m) throws GestorBDClubEsportiuException {
        if (psDelMembre == null) {
            try {
                psDelMembre = conn.prepareStatement("DELETE FROM membre WHERE ID_EQUIP = ? and ID_JUGADOR=?");
                //nom,tipus,id_categoria,id_any
            } catch (SQLException ex) {
                throw new GestorBDClubEsportiuException("Error en preparar sentència psInsertMembre", ex);
            }
        }
        try {
            psDelMembre.setInt(1, m.getIdEquip());
            psDelMembre.setInt(2, m.getIdJugador());

        } catch (SQLException ex) {
            throw new GestorBDClubEsportiuException("Error en intentar inserir al eliminar un membre: " + m.getIdEquip(), ex);
        }
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody

    }

    @Override
    public String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result.toUpperCase();
    }

 
    private String convertToSH1(String pswd) {
        String sha1 = "";
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(pswd.getBytes("UTF-8"));
            sha1 = byteToHex(crypt.digest());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }
        return sha1;
    }
//    private String encryptThisString(String input) {
//        try {
//            // getInstance() method is called with algorithm SHA-1
//            MessageDigest md = MessageDigest.getInstance("SHA-1");
//
//            // digest() method is called
//            // to calculate message digest of the input string
//            // returned as array of byte
//            byte[] messageDigest = md.digest(input.getBytes());
//
//            // Convert byte array into signum representation
//            BigInteger no = new BigInteger(1, messageDigest);
//
//            // Convert message digest into hex value
//            String hashtext = no.toString(16);
//
//            // Add preceding 0s to make it 40 digits long
//            while (hashtext.length() < 40) {
//                hashtext = "0" + hashtext;
//            }
//
//            // return the HashText
//            return hashtext;
//        }
//
//        // For specifying wrong message digest algorithms
//        catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private java.sql.Date DateUtil2DateSql(java.util.Date fecha) {

        return new java.sql.Date(fecha.getTime());
    }

    //
    @Override
    public void afegirJugador(Jugador j) throws GestorBDClubEsportiuException {
        if (psInsertJugador == null) {
            try {
                psInsertJugador = conn.prepareStatement("INSERT INTO jugador(nom,cognoms,sexe,data_naix,idlegal,iban,any_fi_revisio,adreca,foto) VALUES (?,?,?,?,?,?,?,?,?)");
                //nom,tipus,id_categoria,id_any
            } catch (SQLException ex) {
                throw new GestorBDClubEsportiuException("Error en preparar sentència psInsertJugador", ex);
            }
        }
        try {
            psInsertJugador.setString(1, j.getNom());
            psInsertJugador.setString(2, j.getCognoms());
            psInsertJugador.setString(3, Character.toString(j.getSexe()));
            psInsertJugador.setDate(4, java.sql.Date.valueOf(j.getDataNaixement()));
            psInsertJugador.setString(5, j.getIdLegal());

            psInsertJugador.setString(6, j.getIBAN());
            psInsertJugador.setInt(7, j.getAnyFiRevisio());
            psInsertJugador.setString(8, j.getAdreca());
            psInsertJugador.setString(9, j.getFoto());
            psInsertJugador.executeUpdate();
        } catch (SQLException ex) {
            throw new GestorBDClubEsportiuException("Error en intentar inserir el Jugador " + j.getNom(), ex);
        }
    }

//       private int id;
//    private String nom;
//    private String cognoms;
//    private char sexe;
//    private Date dataNaixement;
//    private String idLegal;
//    private String IBAN;
//    private int anyFiRevisio;
//    private String adreca;
//    private String foto;
    //
    @Override
    public void modificarJugador(Jugador j) throws GestorBDClubEsportiuException {
        if (psUpdateJugador == null) {
            try {
                psUpdateJugador = conn.prepareStatement("UPDATE jugador SET nom=? , cognoms=?, sexe=?, data_Naix=?,idLegal=?, IBAN=?, any_Fi_Revisio=?,adreca=?, foto=? WHERE id=?");
                //nom,tipus,id_categoria,id_any
            } catch (SQLException ex) {
                throw new GestorBDClubEsportiuException("Error en preparar sentència psUpdateJugador", ex);
            }
        }
        try {
            psUpdateJugador.setString(1, j.getNom());
            psUpdateJugador.setString(2, j.getCognoms());
            psUpdateJugador.setString(3, Character.toString(j.getSexe()));
            psUpdateJugador.setDate(4, java.sql.Date.valueOf(j.getDataNaixement()));
            psUpdateJugador.setString(5, j.getIdLegal());
            psUpdateJugador.setString(6, j.getIBAN());
            psUpdateJugador.setInt(7, j.getAnyFiRevisio());
            psUpdateJugador.setString(8, j.getAdreca());
            psUpdateJugador.setString(9, j.getFoto());
            psUpdateJugador.setInt(10, j.getId());
            psUpdateJugador.executeUpdate();
        } catch (SQLException ex) {
            throw new GestorBDClubEsportiuException("Error en intentar modificar el Jugador " + j.getNom(), ex);
        }
    }

    @Override
    public void eliminarJugador(Jugador j) throws GestorBDClubEsportiuException {
        if (psDelJugador == null) {
            try {
                psDelJugador = conn.prepareStatement("DELETE FROM jugador WHERE id=?");
                //nom,tipus,id_categoria,id_any
            } catch (SQLException ex) {
                throw new GestorBDClubEsportiuException("Error en preparar sentència psDelJugador", ex);
            }
        }
        try {

            psDelJugador.setInt(1, j.getId());
            psDelJugador.executeUpdate();
        } catch (SQLException ex) {
            throw new GestorBDClubEsportiuException("Error en intentar eliminar el Jugador " + j.getNom(), ex);
        }

    }

    @Override
    public void eliminarGrupJugador(List<Integer> ll) throws GestorBDClubEsportiuException {
        if (psDelListJugador == null) {
            try {
                psDelListJugador = conn.prepareStatement("DELETE FROM jugador WHERE id = ?");
            } catch (SQLException ex) {
                throw new GestorBDClubEsportiuException("Error en preparar sentència psDelListJugador", ex);
            }
        }
        Savepoint sp = null;
        try {
            sp = conn.setSavepoint();
            int q = 0;
            for (Integer codi : ll) {
                psDelListJugador.setInt(1, codi);
                q = q + psDelListJugador.executeUpdate();
            }
            if (q < ll.size()) {
                conn.rollback(sp);
                throw new GestorBDClubEsportiuException("No s'ha efectuat l'eliminació per què "
                        + "no s'han trobat tots els jugadors que es demanava d'eliminar");
            }
        } catch (SQLException ex) {
            if (sp != null) {
                try {
                    conn.rollback(sp);
                } catch (SQLException ex1) {
                }
            }
            throw new GestorBDClubEsportiuException("Error en eliminar els jugadors de codi indicat", ex);
        }

    }
    
  

    public LocalDate validarData(String stringData) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        try {
            LocalDate data = LocalDate.parse(stringData, format);
            return data;
        } catch (DateTimeParseException e) {
            // Maneja el error de formato
            System.out.println("Error: El format de la data es incorrecte");
            return null;
        }
    }
    

    @Override
    public void confirmarCanvis() throws GestorBDClubEsportiuException {
        try {
            conn.commit();
        } catch (SQLException ex) {
            throw new GestorBDClubEsportiuException("Error en confirmar canvis", ex);
        }
    }

    @Override
    public void desferCanvis() throws GestorBDClubEsportiuException {
        try {
            conn.rollback();
        } catch (SQLException ex) {
            throw new GestorBDClubEsportiuException("Error en desfer canvis", ex);
        }
    }

    @Override
    public void afegirTemporada(Temporada t) throws GestorBDClubEsportiuException {
                if (psInsertTemporada == null) {
            try {
                psInsertTemporada = conn.prepareStatement("INSERT INTO TEMPORADA (ANY_TEMP) VALUES (?)");
            } catch (SQLException ex) {
                throw new GestorBDClubEsportiuException("Error en preparar sentència psInsertTemporada", ex);
            }
        }
        try {
            psInsertTemporada.setInt(1, t.getAny_temp());

            psInsertTemporada.executeUpdate();
            
        } catch (SQLException ex) {
            throw new GestorBDClubEsportiuException("Error en intentar insertar la Temporada" + t.getAny_temp(), ex);
        }
    }




}
