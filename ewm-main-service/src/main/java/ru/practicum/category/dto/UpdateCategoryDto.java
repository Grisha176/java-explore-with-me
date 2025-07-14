package ru.practicum.category.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCategoryDto {

    private String name;

    public boolean hasName(){
        return this.name!=null;
    }
}
