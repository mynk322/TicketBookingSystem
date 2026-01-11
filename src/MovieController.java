import enums.City;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovieController {

    Map<City, List<Movie>> cityVsMovies;
    List<Movie> allMovies;

    public MovieController() {
        cityVsMovies = new HashMap<>();
        allMovies = new ArrayList<>();
    }

    public void addMovie(Movie movie, City city) {
        allMovies.add(movie);
        List<Movie> moviesInCity = cityVsMovies.getOrDefault(city, new ArrayList<>());
        moviesInCity.add(movie);
        cityVsMovies.put(city, moviesInCity);
    }
    public Movie getMovieByName(String movieName) {
        for (Movie movie : allMovies) {
            if ((movie.getTitle()).equals(movieName)) {
                return movie;
            }
        }
        return null;
    }
    public List<Movie> getMoviesByCity(City city) {
        return cityVsMovies.get(city);
    }


    public void removeMovie(Movie movie, City city) {
        allMovies.remove(movie);
        List<Movie> moviesInCity = cityVsMovies.get(city);
        moviesInCity.remove(movie);
        cityVsMovies.put(city, moviesInCity);
    }

    public void updateMovie(Movie movie, City city) {
        removeMovie(movie, city);
        addMovie(movie, city);
    }

    public void updateMovieById(int movieId, Movie movie) {
        for (Movie movieObj : allMovies) {
            if (movieObj.getMovieId() == movieId) {
                movieObj.setTitle(movieObj.getTitle());
                movieObj.setDurationInMinutes(movieObj.getDurationInMinutes());
                movieObj.setGenre(movieObj.getGenre());
                movieObj.setLanguage(movieObj.getLanguage());
                break;
            }
        }
    }

    public void deleteMovieById(int movieId) {
        for (Movie movieObj : allMovies) {
            if (movieObj.getMovieId() == movieId) {
                allMovies.remove(movieObj);
                break;
            }
        }
    }

    public Map<City, List<Movie>> getCityVsMovies() {
        return cityVsMovies;
    }

    public void setCityVsMovies(Map<City, List<Movie>> cityVsMovies) {
        this.cityVsMovies = cityVsMovies;
    }

    public List<Movie> getAllMovies() {
        return allMovies;
    }

    public void setAllMovies(List<Movie> allMovies) {
        this.allMovies = allMovies;
    }

}
