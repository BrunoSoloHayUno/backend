package com.backendwebscraper.backend.repositories;

import com.backendwebscraper.backend.entities.WebPage;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class SearchRepositoryImp implements SearchRepository{

    /*la anotacion Transactional
    * sirve para indicarle a Spring que
    * haga una transaccion de SQL unicamente
    * en este metodo
    * en otras palabras envualve todas las consultas que resive
    * en una sola en este caso solo esta resiviendo una (explicacion no completa)*/


    /*esta notacion y clase
    * nos permite cargar la clase (entityManager) automaticamente con la
    * conexion a la base de datos
    * y gracias a esto podemos hacer la consulta como nosotros queramos*/
    @PersistenceContext
    EntityManager entityManager;

    @Override
    public WebPage getPorUrl(String url) {
        String query = "FROM WebPage WHERE url = :url";
        List<WebPage> list = entityManager.createQuery(query)
                .setParameter("url", url)
                .getResultList();
        return list.size() == 0 ? null : list.get(0);
    }

    @Override
    public List<WebPage> getLinksAIndexar() {
        String query = "FROM WebPage WHERE titulo is null AND descripcion is null";
        return entityManager.createQuery(query)
                .setMaxResults(100)
                .getResultList();
    }

    @Transactional
    @Override
    public List<WebPage> search(String textSearch) {
        /*lo que pasa con la consulta aca abajo
        * son 2 cosas primero no es SQL normal si no
        * que es el SQL de Hibernate por eso la sintaxy
        * es diferente, 2do no estamos apuntando a la tabla
        * en nuestra base de datos si no a nuestra clase dentro
        * de nuestro proyecto
        *
        * el SQL de Hibernate es como un puente intermedio entre la base de datos
        * y nuestro proyecto.*/
        String query = "FROM WebPage WHERE descripcion like :textSearch";
        return entityManager.createQuery(query)
                .setParameter("textSearch", "%" + textSearch + "%")
                .getResultList();
    }

    @Transactional
    @Override
    public void save(WebPage webPage) {
        entityManager.merge(webPage);
    }

    @Override
    public boolean existe(String url) {
        return getPorUrl(url) != null;
    }
}
