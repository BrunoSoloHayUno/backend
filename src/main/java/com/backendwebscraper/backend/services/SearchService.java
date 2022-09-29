package com.backendwebscraper.backend.services;

import com.backendwebscraper.backend.entities.WebPage;
import com.backendwebscraper.backend.repositories.SearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    @Autowired
    private SearchRepository repository;

    public List<WebPage> search(String textSearch){
        return repository.search(textSearch);
    }

    public void save(WebPage webPage){
        repository.save(webPage);
    }


    public boolean existe(String link) {
        return repository.existe(link);
    }

    public List<WebPage> getLinksAIndexar(){
        return repository.getLinksAIndexar();
    }


}
