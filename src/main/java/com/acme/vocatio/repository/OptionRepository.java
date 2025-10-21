package com.acme.vocatio.repository;

import com.acme.vocatio.model.Option;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionRepository extends JpaRepository<Option, Integer> {
}
