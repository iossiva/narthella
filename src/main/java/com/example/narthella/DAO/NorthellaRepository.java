package com.example.narthella.DAO;

import com.example.narthella.Model.Dummy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
@Repository
public interface NorthellaRepository extends CrudRepository<Dummy,Integer> {

    @Query(nativeQuery = true)
    Map<String,Object> getNorthellaJson();
}
