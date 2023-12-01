package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class Review {

    private Long reviewId;

    @NotBlank(message = "Необходимо передать содержание отзыва")
    @Size(min = 10, max = 200, message = "Количество симоволов в содержании не может быть меньше 10 и больше 200")
    private String content;

    @NotNull(message = "Нужно указать, отзыв позитивный или негативный")
    private Boolean isPositive;

    @NotNull(message = "Нужно указать ID пользователя, оставившего отзыв")
    private Long userId;

    @NotNull(message = "Нужно указать ID фильма, к которому составлен отзыв")
    private Long filmId;

    private int useful = 0;
}
