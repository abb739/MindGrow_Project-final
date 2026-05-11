<?php

namespace App\Tests\Service;

use App\Entity\Therapeute;
use App\Service\TherapeuteBusinessService;
use PHPUnit\Framework\TestCase;

class TherapeuteBusinessServiceTest extends TestCase
{
    private TherapeuteBusinessService $service;

    protected function setUp(): void
    {
        $this->service = new TherapeuteBusinessService();
    }

    public function testIsApproved(): void
    {
        $therapeute = new Therapeute();
        $therapeute->setStatutCertificat('Approuvé');
        $this->assertTrue($this->service->isApproved($therapeute));
    }

    public function testIsNotApproved(): void
    {
        $therapeute = new Therapeute();
        $therapeute->setStatutCertificat('En attente');
        $this->assertFalse($this->service->isApproved($therapeute));
    }

    public function testHasCompleteContactInfo(): void
    {
        $therapeute = new Therapeute();
        $therapeute->setEmail('test@example.com');
        $therapeute->setTelephone('21612345678');
        $this->assertTrue($this->service->hasCompleteContactInfo($therapeute));
    }

    public function testHasIncompleteContactInfo(): void
    {
        $therapeute = new Therapeute();
        $therapeute->setEmail('test@example.com');
        // Missing telephone
        $this->assertFalse($this->service->hasCompleteContactInfo($therapeute));
    }
}
