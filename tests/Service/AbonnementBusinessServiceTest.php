<?php

namespace App\Tests\Service;

use App\Entity\Abonnement;
use App\Service\AbonnementBusinessService;
use PHPUnit\Framework\TestCase;

class AbonnementBusinessServiceTest extends TestCase
{
    private AbonnementBusinessService $service;

    protected function setUp(): void
    {
        $this->service = new AbonnementBusinessService();
    }

    public function testIsPremium(): void
    {
        $abonnement = new Abonnement();
        $abonnement->setPrix("150.00");
        $this->assertTrue($this->service->isPremium($abonnement));
    }

    public function testIsNotPremium(): void
    {
        $abonnement = new Abonnement();
        $abonnement->setPrix("49.99");
        $this->assertFalse($this->service->isPremium($abonnement));
    }

    public function testIsLongTerm(): void
    {
        $abonnement = new Abonnement();
        $abonnement->setDureeMois(12);
        $this->assertTrue($this->service->isLongTerm($abonnement));
    }

    public function testIsNotLongTerm(): void
    {
        $abonnement = new Abonnement();
        $abonnement->setDureeMois(3);
        $this->assertFalse($this->service->isLongTerm($abonnement));
    }
}
