<?php

namespace App\Service;

use App\Entity\Abonnement;

class AbonnementBusinessService
{
    /**
     * Rule 1: A subscription is considered "Premium" if its price is > 100.
     */
    public function isPremium(Abonnement $abonnement): bool
    {
        $prix = (float) $abonnement->getPrix();
        return $prix > 100.0;
    }

    /**
     * Rule 2: A subscription is "Long Term" if it lasts more than 6 months.
     */
    public function isLongTerm(Abonnement $abonnement): bool
    {
        $duree = $abonnement->getDureeMois();
        return $duree !== null && $duree > 6;
    }
}
