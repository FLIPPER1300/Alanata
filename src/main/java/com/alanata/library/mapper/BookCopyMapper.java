package com.alanata.library.mapper;

import com.alanata.library.dto.response.BookCopyResponse;
import com.alanata.library.entity.Book;
import com.alanata.library.entity.BookCopy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookCopyMapper {

    BookCopyResponse toResponse(BookCopy bookCopy);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "book", source = "book")
    @Mapping(target = "available", constant = "true")
    BookCopy toEntity(Book book);
}
