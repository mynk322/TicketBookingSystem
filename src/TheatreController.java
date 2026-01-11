import enums.City;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TheatreController {

    Map<City, List<Theatre>> cityVsTheatre;
    List<Theatre> allTheatre;

    public TheatreController() {
        this.cityVsTheatre = new HashMap<>();
        this.allTheatre = new ArrayList<>();
    }

    public void addTheatre(Theatre theatre, City city) {
        allTheatre.add(theatre);
        List<Theatre> theatres = cityVsTheatre.getOrDefault(city, new ArrayList<>());
        theatres.add(theatre);
        cityVsTheatre.put(city, theatres);
    }

    public Map<Theatre, List<Show>> getAllShows(Movie movie, City city) {
        Map<Theatre, List<Show>> theatreVsShows = new HashMap<>();
        List<Theatre> theatres = cityVsTheatre.get(city);
        // filter the theatres which run this movie
        for (Theatre theatre : theatres) {
            List<Show> givenMovieShows = new ArrayList<>();
            List<Show> shows = theatre.getShows();
            for (Show show : shows) {
                if (show.movie.getMovieId() == movie.getMovieId()) {
                    givenMovieShows.add(show);
                }
            }
            if (!givenMovieShows.isEmpty()) {
                theatreVsShows.put(theatre, givenMovieShows);
            }
        }
        return theatreVsShows;
    }

    public Map<City, List<Theatre>> getCityVsTheatre() {
        return cityVsTheatre;
    }

    public void setCityVsTheatre(Map<City, List<Theatre>> cityVsTheatre) {
        this.cityVsTheatre = cityVsTheatre;
    }

    public List<Theatre> getAllTheatre() {
        return allTheatre;
    }

    public void setAllTheatre(List<Theatre> allTheatre) {
        this.allTheatre = allTheatre;
    }
}
