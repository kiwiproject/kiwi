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
@TypeDef(name = "jsonb", typeClass = JSONBUserType.class)
@Getter
@Setter
@SuppressWarnings("JpaDataSourceORMInspection")
class SampleJsonbEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text_col")
    private String textCol;

    @Type(type = "jsonb")
    @Column(name = "jsonb_col")
    private String jsonbCol;
}
