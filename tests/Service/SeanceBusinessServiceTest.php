<?php

namespace App\Tests\Service;

use App\Entity\Seance;
use App\Service\SeanceBusinessService;
use PHPUnit\Framework\TestCase;

class SeanceBusinessServiceTest extends TestCase
{
    private SeanceBusinessService $service;

    protected function setUp(): void
    {
        $this->service = new SeanceBusinessService();
    }

    public function testDurationValid(): void
    {
        $seance = new Seance();
        $start = new \DateTime('2026-06-01 10:00:00');
        $end = new \DateTime('2026-06-01 12:00:00'); // 2 hours

        // Use reflection to set private date fields if they are private and have no public setters
        // Or check if they have public setters. 
        // In Seance.php, setDateDebut and setDateFin are private.
        // I should probably make them public or use reflection.
        
        $reflection = new \ReflectionClass(Seance::class);
        $propStart = $reflection->getProperty('dateDebut');
        $propStart->setAccessible(true);
        $propStart->setValue($seance, $start);

        $propEnd = $reflection->getProperty('dateFin');
        $propEnd->setAccessible(true);
        $propEnd->setValue($seance, $end);

        $this->assertTrue($this->service->isDurationValid($seance));
    }

    public function testDurationTooShort(): void
    {
        $seance = new Seance();
        $start = new \DateTime('2026-06-01 10:00:00');
        $end = new \DateTime('2026-06-01 10:15:00'); // 15 mins

        $reflection = new \ReflectionClass(Seance::class);
        $propStart = $reflection->getProperty('dateDebut');
        $propStart->setAccessible(true);
        $propStart->setValue($seance, $start);

        $propEnd = $reflection->getProperty('dateFin');
        $propEnd->setAccessible(true);
        $propEnd->setValue($seance, $end);

        $this->assertFalse($this->service->isDurationValid($seance));
    }

    public function testDurationTooLong(): void
    {
        $seance = new Seance();
        $start = new \DateTime('2026-06-01 10:00:00');
        $end = new \DateTime('2026-06-01 15:00:00'); // 5 hours

        $reflection = new \ReflectionClass(Seance::class);
        $propStart = $reflection->getProperty('dateDebut');
        $propStart->setAccessible(true);
        $propStart->setValue($seance, $start);

        $propEnd = $reflection->getProperty('dateFin');
        $propEnd->setAccessible(true);
        $propEnd->setValue($seance, $end);

        $this->assertFalse($this->service->isDurationValid($seance));
    }

    public function testFutureDateValid(): void
    {
        $seance = new Seance();
        $start = new \DateTime('+2 hours');

        $reflection = new \ReflectionClass(Seance::class);
        $propStart = $reflection->getProperty('dateDebut');
        $propStart->setAccessible(true);
        $propStart->setValue($seance, $start);

        $this->assertTrue($this->service->isFutureDateValid($seance));
    }

    public function testFutureDateTooClose(): void
    {
        $seance = new Seance();
        $start = new \DateTime('+30 minutes');

        $reflection = new \ReflectionClass(Seance::class);
        $propStart = $reflection->getProperty('dateDebut');
        $propStart->setAccessible(true);
        $propStart->setValue($seance, $start);

        $this->assertFalse($this->service->isFutureDateValid($seance));
    }
}
