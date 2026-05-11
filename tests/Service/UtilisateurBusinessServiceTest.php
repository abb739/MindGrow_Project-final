<?php

namespace App\Tests\Service;

use App\Entity\Utilisateur;
use App\Service\UtilisateurBusinessService;
use PHPUnit\Framework\TestCase;

class UtilisateurBusinessServiceTest extends TestCase
{
    private UtilisateurBusinessService $service;

    protected function setUp(): void
    {
        $this->service = new UtilisateurBusinessService();
    }

    public function testIsAdmin(): void
    {
        $user = new Utilisateur();
        $user->setRole('ROLE_ADMIN');
        $this->assertTrue($this->service->isAdmin($user));
    }

    public function testIsNotAdmin(): void
    {
        $user = new Utilisateur();
        $user->setRole('ROLE_USER');
        $this->assertFalse($this->service->isAdmin($user));
    }

    public function testPasswordIsSecure(): void
    {
        $user = new Utilisateur();
        $user->setMotDePasse('Secret123!');
        $this->assertTrue($this->service->isPasswordSecure($user));
    }

    public function testPasswordIsNotSecure(): void
    {
        $user = new Utilisateur();
        $user->setMotDePasse('123');
        $this->assertFalse($this->service->isPasswordSecure($user));
    }
}
