<?php

namespace App\Service;

use App\Entity\Programme;

class ProgrammeBusinessService
{
    /**
     * Rule 1: A program description must be at least 20 characters.
     */
    public function isDescriptionUseful(Programme $programme): bool
    {
        $description = $programme->getDescription();
        return $description !== null && mb_strlen($description) >= 20;
    }

    /**
     * Rule 2: A program must have either an image or a video.
     */
    public function hasVisualMedia(Programme $programme): bool
    {
        return !empty($programme->getImage()) || !empty($programme->getVideo());
    }
}
