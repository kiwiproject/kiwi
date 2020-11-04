package org.kiwiproject.hibernate.usertype;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sample_entity")
@TypeDef(name = "textArray", typeClass = TextArrayUserType.class)
@TypeDef(name = "bigintArray", typeClass = BigintArrayUserType.class)
@Getter
@Setter
@SuppressWarnings("JpaDataSourceORMInspection")
class SampleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text_col")
    private String textCol;

    @Type(type = "textArray")
    @Column(name = "varchar_array_col")
    private String[] varcharArrayCol;

    @Type(type = "textArray")
    @Column(name = "text_array_col")
    private String[] textArrayCol;

    @Type(type = "bigintArray")
    @Column(name = "bigint_array_col")
    private Long[] bigintArrayCol;
}
