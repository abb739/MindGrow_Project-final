<?php

namespace App\Tests\Service;

use App\Entity\Programme;
use App\Service\ProgrammeBusinessService;
use PHPUnit\Framework\TestCase;

class ProgrammeBusinessServiceTest extends TestCase
{
    private ProgrammeBusinessService $service;

    protected function setUp(): void
    {
        $this->service = new ProgrammeBusinessService();
    }

    public function testDescriptionUseful(): void
    {
        $programme = new Programme();
        $programme->setDescription("Ceci est une description assez longue pour être utile.");
        $this->assertTrue($this->service->isDescriptionUseful($programme));
    }

    public function testDescriptionNotUseful(): void
    {
        $programme = new Programme();
        $programme->setDescription("Trop court.");
        $this->assertFalse($this->service->isDescriptionUseful($programme));
    }

    public function testHasVisualMediaWithImage(): void
    {
        $programme = new Programme();
        $programme->setImage("photo.jpg");
        $this->assertTrue($this->service->hasVisualMedia($programme));
    }

    public function testHasVisualMediaWithVideo(): void
    {
        $programme = new Programme();
        $programme->setVideo("video.mp4");
        $this->assertTrue($this->service->hasVisualMedia($programme));
    }

    public function testHasNoVisualMedia(): void
    {
        $programme = new Programme();
        $this->assertFalse($this->service->hasVisualMedia($programme));
    }
}
