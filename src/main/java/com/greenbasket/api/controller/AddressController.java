package com.greenbasket.api.controller;

import com.greenbasket.api.model.Address;
import com.greenbasket.api.model.User;
import com.greenbasket.api.payload.request.AddressRequest;
import com.greenbasket.api.payload.response.MessageResponse;
import com.greenbasket.api.repository.AddressRepository;
import com.greenbasket.api.repository.UserRepository;
import com.greenbasket.api.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    @Autowired
    AddressRepository addressRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getAllAddresses() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            List<Address> addresses = addressRepository.findByUserId(userDetails.getId());
            
            return new ResponseEntity<>(addresses, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<?> createAddress(@Valid @RequestBody AddressRequest addressRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            Optional<User> userOptional = userRepository.findById(userDetails.getId());
            
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("User not found"));
            }
            
            User user = userOptional.get();
            
            // If this address is set as default, unset any existing default address
            if (addressRequest.isDefault()) {
                Optional<Address> defaultAddressOptional = addressRepository.findByUserIdAndIsDefaultTrue(user.getId());
                
                if (defaultAddressOptional.isPresent()) {
                    Address defaultAddress = defaultAddressOptional.get();
                    defaultAddress.setDefault(false);
                    addressRepository.save(defaultAddress);
                }
            }
            
            Address address = new Address();
            address.setType(addressRequest.getType());
            address.setName(addressRequest.getName());
            address.setAddress(addressRequest.getAddress());
            address.setCity(addressRequest.getCity());
            address.setState(addressRequest.getState());
            address.setZip(addressRequest.getZip());
            address.setPhone(addressRequest.getPhone());
            address.setDefault(addressRequest.isDefault());
            address.setUser(user);
            
            Address savedAddress = addressRepository.save(address);
            
            return new ResponseEntity<>(savedAddress, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable("id") Long id, @Valid @RequestBody AddressRequest addressRequest) {
        Optional<Address> addressData = addressRepository.findById(id);

        if (addressData.isPresent()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            Address address = addressData.get();
            
            // Check if the address belongs to the user
            if (!address.getUser().getId().equals(userDetails.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse("You are not authorized to update this address"));
            }
            
            // If this address is being set as default, unset any existing default address
            if (addressRequest.isDefault() && !address.isDefault()) {
                Optional<Address> defaultAddressOptional = addressRepository.findByUserIdAndIsDefaultTrue(userDetails.getId());
                
                if (defaultAddressOptional.isPresent()) {
                    Address defaultAddress = defaultAddressOptional.get();
                    defaultAddress.setDefault(false);
                    addressRepository.save(defaultAddress);
                }
            }
            
            address.setType(addressRequest.getType());
            address.setName(addressRequest.getName());
            address.setAddress(addressRequest.getAddress());
            address.setCity(addressRequest.getCity());
            address.setState(addressRequest.getState());
            address.setZip(addressRequest.getZip());
            address.setPhone(addressRequest.getPhone());
            address.setDefault(addressRequest.isDefault());
            
            return new ResponseEntity<>(addressRepository.save(address), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable("id") Long id) {
        try {
            Optional<Address> addressData = addressRepository.findById(id);
            
            if (!addressData.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            Address address = addressData.get();
            
            // Check if the address belongs to the user
            if (!address.getUser().getId().equals(userDetails.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse("You are not authorized to delete this address"));
            }
            
            addressRepository.deleteById(id);
            
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
