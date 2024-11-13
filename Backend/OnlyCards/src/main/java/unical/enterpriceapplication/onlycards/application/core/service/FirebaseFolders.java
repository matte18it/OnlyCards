package unical.enterpriceapplication.onlycards.application.core.service;

import lombok.Getter;

@Getter
public enum FirebaseFolders{

    /** Folder for storing user profiles */
    USERS_PROFILE("usersProfile/"),
    
    /** Folder for storing product types */
    PRODUCT_TYPE("productTypes/"),
    
    /** Folder for storing products */
    PRODUCT("products/");

    private final String folderName;

    FirebaseFolders(String folderName) {
        this.folderName = folderName;
    }

}
