package com.example.proyecto_hibernate.CRUD;

import com.example.proyecto_hibernate.classes.ParteIncidencia;
import com.example.proyecto_hibernate.util.Alerta;
import com.example.proyecto_hibernate.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class PartesCRUD {
    SessionFactory factory = HibernateUtil.getSessionFactory();

    public ArrayList<ParteIncidencia> obtenerPartes(){
        Transaction transaction;
        ArrayList<ParteIncidencia> listaPartes = new ArrayList<>();
        try(Session session = factory.openSession()){
            transaction = session.beginTransaction();
            List<ParteIncidencia> parteIncidencias = session.createQuery("from ParteIncidencia", ParteIncidencia.class).getResultList();
            listaPartes.addAll(parteIncidencias);
            transaction.commit();
        }catch (Exception e){
            Alerta.mensajeError(null, e.getMessage());
            e.printStackTrace();
        }
        return listaPartes;
    }


    public void crearParte(ParteIncidencia parteIncidencia) {
        Transaction transaction = null;
        try (Session session = factory.openSession()) {
            transaction = session.beginTransaction();

            session.save(parteIncidencia);

            transaction.commit();
        } catch (Exception e) {
            Alerta.mensajeError(null, e.getMessage());
        }
    }//insertarParte
}
