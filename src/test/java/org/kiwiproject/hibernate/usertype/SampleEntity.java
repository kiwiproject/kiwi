package org.kiwiproject.hibernate.usertype;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "sample_entity")
@Getter
@Setter
@SuppressWarnings("JpaDataSourceORMInspection")
class SampleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text_col")
    private String textCol;

    @Type(TextArrayUserType.class)
    @Column(name = "varchar_array_col")
    private String[] varcharArrayCol;

    @Type(TextArrayUserType.class)
    @Column(name = "text_array_col")
    private String[] textArrayCol;

    @Type(BigintArrayUserType.class)
    @Column(name = "bigint_array_col")
    private Long[] bigintArrayCol;
}
