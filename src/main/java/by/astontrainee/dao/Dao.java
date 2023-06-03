package by.astontrainee.dao;

import java.util.List;

/**
 * @author Alex Mikhalevich
 */
public interface Dao <T>{

    List<T> selectAll();
    T selectOne(int id);
    T update(T t);
    void delete(int id);
    T insert(T t);
}
