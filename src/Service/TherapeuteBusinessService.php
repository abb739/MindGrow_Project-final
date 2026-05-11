<?php

namespace App\Service;

use App\Entity\Therapeute;

class TherapeuteBusinessService
{
    /**
     * Rule 1: A therapist is approved only if their certificate status is 'Approuvé'.
     */
    public function isApproved(Therapeute $therapeute): bool
    {
        return $therapeute->getStatutCertificat() === 'Approuvé';
    }

    /**
     * Rule 2: A therapist must have both an email and a phone number for valid contact.
     */
    public function hasCompleteContactInfo(Therapeute $therapeute): bool
    {
        return !empty($therapeute->getEmail()) && !empty($therapeute->getTelephone());
    }
}
