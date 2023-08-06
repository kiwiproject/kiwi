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
class SampleJsonbEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text_col")
    private String textCol;

    @Type(value = JSONBUserType.class)
    @Column(name = "jsonb_col")
    private String jsonbCol;
}
