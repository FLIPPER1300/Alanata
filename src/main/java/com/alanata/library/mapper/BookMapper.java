package com.alanata.library.mapper;

import com.alanata.library.dto.request.BookCreateRequest;
import com.alanata.library.dto.request.BookUpdateRequest;
import com.alanata.library.dto.response.BookDetailsResponse;
import com.alanata.library.dto.response.BookListItemResponse;
import com.alanata.library.entity.Book;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = BookCopyMapper.class)
public interface BookMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "copies", ignore = true)
    Book toEntity(BookCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "copies", ignore = true)
    void updateEntity(BookUpdateRequest request, @MappingTarget Book book);

    BookListItemResponse toListItemResponse(Book book);

    BookDetailsResponse toDetailsResponse(Book book);
}
