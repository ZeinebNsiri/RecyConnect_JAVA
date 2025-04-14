package services;

import entities.Cours;

import java.sql.SQLException;
import java.util.List;

public class CoursService implements IService<Cours> {
    @Override
    public List<Cours> displayList() throws SQLException {
        return List.of();
    }

    @Override
    public void add(Cours cours) throws SQLException {

    }

    @Override
    public void update(Cours cours) throws SQLException {

    }

    @Override
    public void delete(Cours cours) throws SQLException {

    }
}
