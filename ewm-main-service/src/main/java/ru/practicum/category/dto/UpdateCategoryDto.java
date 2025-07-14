package ru.practicum.category.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCategoryDto {

    private String name;

    public boolean hasName(){
        return this.name!=null;
    }
}
