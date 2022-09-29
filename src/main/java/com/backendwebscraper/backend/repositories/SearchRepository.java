package com.backendwebscraper.backend.repositories;

import com.backendwebscraper.backend.entities.WebPage;

import java.util.List;

public interface SearchRepository {

    WebPage getPorUrl(String url);

    List<WebPage> getLinksAIndexar();

    List<WebPage> search(String textSearch);

    void save(WebPage webPage);

    boolean existe(String link);
}
