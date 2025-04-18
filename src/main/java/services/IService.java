package services;

import java.sql.SQLException;
import java.util.List;

public interface IService <T>{
    List<T> displayList()throws SQLException;
    void add(T t) throws SQLException;
    void delete(T t) throws SQLException;
    void update(T t) throws SQLException;
}
