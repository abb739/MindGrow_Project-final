<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260510023346 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Add updated_at column to favori_programme table for Gedmo Timestampable';
    }

    public function up(Schema $schema): void
    {
        // Add updated_at column to favori_programme table
        $this->addSql('ALTER TABLE favori_programme ADD updated_at DATETIME DEFAULT NULL');
    }

    public function down(Schema $schema): void
    {
        // Remove updated_at column from favori_programme table
        $this->addSql('ALTER TABLE favori_programme DROP COLUMN updated_at');
    }
}
