<?php

namespace App\Service;

use App\Entity\Utilisateur;

class UtilisateurBusinessService
{
    /**
     * Rule 1: Check if the user has an Admin role.
     */
    public function isAdmin(Utilisateur $utilisateur): bool
    {
        return $utilisateur->getRole() === 'ROLE_ADMIN' || $utilisateur->getRole() === 'ADMIN';
    }

    /**
     * Rule 2: A password is secure if it's at least 8 characters long.
     */
    public function isPasswordSecure(Utilisateur $utilisateur): bool
    {
        $password = $utilisateur->getMotDePasse();
        return $password !== null && mb_strlen($password) >= 8;
    }
}
