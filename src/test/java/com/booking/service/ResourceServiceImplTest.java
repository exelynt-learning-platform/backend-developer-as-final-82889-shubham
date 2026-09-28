package com.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.booking.dto.request.ResourceRequest;
import com.booking.dto.response.ResourceResponse;
import com.booking.entity.Resource;
import com.booking.exeption.ResourceNotFoundException;
import com.booking.repository.ResourceRepository;
import com.booking.service.impl.ResourceServiceImpl;

@ExtendWith(MockitoExtension.class)
class ResourceServiceImplTest {

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ResourceServiceImpl resourceService;


    @Test
    void createResource_shouldCreateResourceSuccessfully() {

        ResourceRequest request = new ResourceRequest();
        request.setName("Conference Room");
        request.setDescription("Large conference room");
        request.setType("ROOM");
        request.setPrice(new BigDecimal("1000.00"));
        request.setAvailable(true);

        Resource savedResource = Resource.builder()
                .id(1L)
                .name("Conference Room")
                .description("Large conference room")
                .type("ROOM")
                .price(new BigDecimal("1000.00"))
                .available(true)
                .build();

        when(resourceRepository.save(any(Resource.class)))
                .thenReturn(savedResource);

        ResourceResponse response =
                resourceService.createResource(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Conference Room", response.getName());
        assertEquals("ROOM", response.getType());
        assertEquals(
                new BigDecimal("1000.00"),
                response.getPrice()
        );
        assertTrue(response.isAvailable());

        verify(resourceRepository).save(any(Resource.class));
    }


    @Test
    void getResourceById_shouldReturnResource_whenResourceExists() {

        Resource resource = Resource.builder()
                .id(1L)
                .name("Conference Room")
                .description("Meeting room")
                .type("ROOM")
                .price(new BigDecimal("500"))
                .available(true)
                .build();

        when(resourceRepository.findById(1L))
                .thenReturn(Optional.of(resource));

        ResourceResponse response =
                resourceService.getResourceById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Conference Room", response.getName());

        verify(resourceRepository).findById(1L);
    }


    @Test
    void getResourceById_shouldThrowException_whenResourceDoesNotExist() {

        when(resourceRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> resourceService.getResourceById(999L)
        );

        verify(resourceRepository).findById(999L);
    }


    @Test
    void getAllResources_shouldReturnAllResources() {

        List<Resource> resources = List.of(
                Resource.builder()
                        .id(1L)
                        .name("Room A")
                        .type("ROOM")
                        .price(new BigDecimal("500"))
                        .available(true)
                        .build(),

                Resource.builder()
                        .id(2L)
                        .name("Vehicle A")
                        .type("VEHICLE")
                        .price(new BigDecimal("1500"))
                        .available(true)
                        .build()
        );

        when(resourceRepository.findAll())
                .thenReturn(resources);

        List<ResourceResponse> response =
                resourceService.getAllResources();

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("Room A", response.get(0).getName());
        assertEquals("Vehicle A", response.get(1).getName());

        verify(resourceRepository).findAll();
    }


    @Test
    void updateResource_shouldUpdateResourceSuccessfully() {

        Resource existingResource = Resource.builder()
                .id(1L)
                .name("Old Room")
                .description("Old description")
                .type("ROOM")
                .price(new BigDecimal("500"))
                .available(true)
                .build();

        ResourceRequest request = new ResourceRequest();
        request.setName("Updated Room");
        request.setDescription("Updated description");
        request.setType("ROOM");
        request.setPrice(new BigDecimal("800"));
        request.setAvailable(false);

        when(resourceRepository.findById(1L))
                .thenReturn(Optional.of(existingResource));

        when(resourceRepository.save(existingResource))
                .thenReturn(existingResource);

        ResourceResponse response =
                resourceService.updateResource(1L, request);

        assertEquals("Updated Room", response.getName());
        assertEquals("Updated description", response.getDescription());
        assertEquals(new BigDecimal("800"), response.getPrice());
        assertFalse(response.isAvailable());

        verify(resourceRepository).findById(1L);
        verify(resourceRepository).save(existingResource);
    }


    @Test
    void updateResource_shouldThrowException_whenResourceDoesNotExist() {

        ResourceRequest request = new ResourceRequest();

        when(resourceRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> resourceService.updateResource(999L, request)
        );

        verify(resourceRepository, never())
                .save(any(Resource.class));
    }


    @Test
    void deleteResource_shouldDeleteResourceSuccessfully() {

        Resource resource = Resource.builder()
                .id(1L)
                .name("Room")
                .type("ROOM")
                .price(new BigDecimal("500"))
                .available(true)
                .build();

        when(resourceRepository.findById(1L))
                .thenReturn(Optional.of(resource));

        resourceService.deleteResource(1L);

        verify(resourceRepository).findById(1L);
        verify(resourceRepository).delete(resource);
    }


    @Test
    void deleteResource_shouldThrowException_whenResourceDoesNotExist() {

        when(resourceRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> resourceService.deleteResource(999L)
        );

        verify(resourceRepository, never())
                .delete(any(Resource.class));
    }
}