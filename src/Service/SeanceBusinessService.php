<?php

namespace App\Service;

use App\Entity\Seance;

class SeanceBusinessService
{
    public function isDurationValid(Seance $seance): bool
    {
        $start = $seance->getDateDebut();
        $end = $seance->getDateFin();

        if (!$start || !$end) {
            return false;
        }

        $duration = $end->getTimestamp() - $start->getTimestamp();
        
        // 30 mins = 1800s, 4 hours = 14400s
        return $duration >= 1800 && $duration <= 14400;
    }

   
    public function isFutureDateValid(Seance $seance): bool
    {
        $start = $seance->getDateDebut();
        if (!$start) {
            return false;
        }

        $oneHourFromNow = new \DateTime('+1 hour');
        return $start > $oneHourFromNow;
    }
}
