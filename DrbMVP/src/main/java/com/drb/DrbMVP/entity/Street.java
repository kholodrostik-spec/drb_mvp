package com.drb.DrbMVP.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "streets")
@Getter
@Setter
public class Street {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String highway;

    @Column(name = "length_m")
    private Double lengthM;

}
