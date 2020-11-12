package org.kiwiproject.spring.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class OtherTestBean {
    String name;
    int value;
    SampleTestBean sampleTestBean;
}
