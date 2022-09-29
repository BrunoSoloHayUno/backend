package com.backendwebscraper.backend.services;

import com.backendwebscraper.backend.entities.WebPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hibernate.internal.util.StringHelper.isBlank;

@Service
public class SpiderService {

    @Autowired
    private SearchService searchService;

    public void indexWebPages() {
        List<WebPage> linksAIndexar = searchService.getLinksAIndexar();
        linksAIndexar.stream().parallel().forEach(webPage -> {
            try {
                System.out.println("Indexando");
                indexWebPage(webPage);
            } catch (Exception e) {

            }
        });
    }

    private void indexWebPage(WebPage webPage) throws Exception{
            String url = webPage.getUrl();
            System.out.println(url);
            String contenido = getContenidoWeb(url);
            if (isBlank(contenido)){
                return;
            }

            indexarYGuardarWebPage(webPage, contenido);

            System.out.println("Dominio: " + getDominio(url));
            guardarLinks(getDominio(url), contenido);
    }

    private String getDominio(String url) {
        String[] aux = url.split("/");
        return aux[0] + "//" + aux[2];
    }

    private void guardarLinks(String dominio, String contenido) {
        System.out.println("Links Guardados");
        List<String> links = getLinks(dominio, contenido);
        System.out.println("Links: " + links);
        links.stream().filter(link -> !searchService.existe(link))
                .map(link -> new WebPage(link))
                .forEach(webPage -> searchService.save(webPage));
    }

    public List<String> getLinks(String dominio, String contenido){
        List<String> links = new ArrayList<>();

        String[] splitHref = contenido.split("href=\"");
        List<String> listHref = Arrays.asList(splitHref);
        //listHref.remove(0);

        listHref.forEach(strHref -> {
            String[] aux = strHref.split("\"");
            links.add(aux[0]);
        });
        return limpiarLinks(dominio, links);
    }

    private List<String> limpiarLinks(String dominio, List<String> links){
        String[] estensionesExcluidas = new String[]{"ccs","js","json","jpg","png","woff2"};


        List<String> linksResultado = links.stream()
                .filter(link -> Arrays.stream(estensionesExcluidas).noneMatch(link::endsWith))
                .map(link -> link.startsWith("/") ? dominio + link : link)
                .filter(link -> link.startsWith("http"))
                .collect(Collectors.toList());


        /*con esto se evita links repetidos:*/
        List<String> linksUnicos = new ArrayList<>();
        linksUnicos.addAll(new HashSet<>(linksResultado));

        return linksResultado;
    }

    private void indexarYGuardarWebPage(WebPage webPage, String contenido) {
        String titulo = getTitulo(contenido);
        String descripcion = getDescripcion(contenido);

        webPage.setDescripcion(descripcion);
        webPage.setTitulo(titulo);

        System.out.println("Guardar: " + webPage);
        searchService.save(webPage);
    }

    public String getTitulo(String contenido){
        String[] aux = contenido.split("<title>");
        String[] aux2 = aux[1].split("</title>");
        return aux2[0];
    }

    public String getDescripcion(String contenido){
        String[] aux = contenido.split("<meta name=\"description\" content=\"");
        String[] aux2 = aux[1].split("\">");
        return aux2[0];
    }

    private String getContenidoWeb(String link) {
        try {
            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 2) Trae la cabezera de la pagina web para saber en que formato esta codificado.
            String encoding = conn.getContentEncoding();

            /*Para descargar las paginas web que nesesitamos, tenemos que usar un
             * InputStream
             * tambien vamos a usar un InputStreamReader que es un leector de InputStreams
             *
             * el metodo joining lo que hace es agarrar to-do lo que encuentre y lo guarda
             * en un string
             *
             *
             * el metodo lines es lo que nos esta devolviendo un Stream de Strings*/

            InputStream entrada = conn.getInputStream();

            Stream<String> lines = new BufferedReader(new InputStreamReader(entrada)).lines();

            return lines.collect(Collectors.joining());
        } catch (Exception e){
            System.out.print(e.getMessage());
        }
        return "";
    }
}
